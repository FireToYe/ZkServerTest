package com.zhongkk.zk.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yechenglong
 * @create 2018/5/2 10:33
 **/
public class DistributedMuteLock implements DistributedLock {

    public DistributedMuteLock(ZooKeeper zooKeeper) {
        this(DistributedMuteLock.BASE_PATH_DEFAULT, DistributedMuteLock.LOCK_NAME_DEFAULT, DistributedMuteLock.MAX_OUT_TIME_DEFAULT, zooKeeper);
    }

    public DistributedMuteLock(String lockPath, String lockNamePre, Long outTime, ZooKeeper zooKeeper) {
        this.lockPath = lockPath;
        this.lockNamePre = lockNamePre;
        this.outTime = outTime;
        this.zooKeeper = zooKeeper;
        init();
    }

    /**
     * 锁节点存放位置
     */
    private static final String BASE_PATH_DEFAULT = "/lock";
    /**
     * 最大超时时间
     */
    private static final Long MAX_OUT_TIME_DEFAULT = 5000L;

    private static final String LOCK_NAME_DEFAULT = "lockNode";

    private static final int MAX_RETRY_COUNT = 10;
    private String lockPath;

    private String lockNamePre;

    private Long outTime;

    private String currentPath;

    private ZooKeeper zooKeeper;

    public void lock() throws Exception {
        attemptLock(-1,null);
    }

    public void lock(Long time, TimeUnit timeUnit) throws Exception {
        if (time <= 0) {
            throw new Exception("Lock wait for time must greater than 0");
        }

        if (timeUnit == null) {
            throw new Exception("TimeUnit can not be null");
        }
        attemptLock(time,timeUnit);
    }

    public void unLock(){
        deleteNode();
    }

    public boolean tryLock() throws Exception{
        currentPath = createCurrentNode();
        List<String> childList = getChildSortList();
        int index = childList.indexOf(currentPath);
        if(index<0){
            throw new Exception("该节点不存在");
        }
        if(index==0){
            return true;
        }
        return false;
    }

    public void init() {
        try {
            Stat stat = zooKeeper.exists(this.lockPath, false);
            if (stat == null) {
                zooKeeper.create(this.lockPath, "lock-message".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建当前顺序临时节点
     */
    private String createCurrentNode() throws KeeperException, InterruptedException {
        String path = this.lockPath + "/" + this.lockNamePre;
        return zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * 获取子节点顺序
     */
    private List<String> getChildSortList() throws Exception {

        List<String> childrenList = zooKeeper.getChildren(this.lockPath, false);
        if (childrenList != null && !childrenList.isEmpty()) {
            Collections.sort(childrenList, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return getLockNumber(o1).compareTo(getLockNumber(o2));
                }
            });

        }

        return childrenList;
    }

    private String getLockNumber(String child) {
        int index = child.lastIndexOf(this.lockNamePre);
        if (index >= 0) {
            index += this.lockNamePre.length();
            return index <= child.length() ? child.substring(index) : "";
        }
        return child;
    }

    /**
     * 用于在使用完锁之后释放锁，删除节点
     */
    private void deleteNode(){
        if(this.currentPath==null){
            return;
        }
        try {
            zooKeeper.delete(this.currentPath,-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private boolean waitToLock(long startMillis, Long millisToWait) throws Exception {
        boolean hasLock = false;
        boolean isDelete = false;
        try {
            while (!hasLock) {
                List<String> childList = getChildSortList();
                String sequenceNodeName = currentPath.substring(lockPath.length() + 1);
                int index = childList.indexOf(sequenceNodeName);
                if (index < 0) {
                    System.err.println("该节点已意外丢失");
                }
                boolean isGetTheLock = index == 0;

                // 如何判断其它客户端是否已经释放了锁？从子节点列表中获取到比自己次小的哪个节点，并对其建立监听
                String pathToWatch = isGetTheLock ? null : childList.get(index - 1);
                if (isGetTheLock) {
                    System.out.println(currentPath + "获取锁");
                    hasLock = true;
                } else {
                    String preNodeName = lockPath.concat("/").concat(pathToWatch);
                    System.out.println(currentPath + "等待锁");
                    final CountDownLatch countDownLatch = new CountDownLatch(1);
                    Watcher watcher = new Watcher() {
                        public void process(WatchedEvent watchedEvent) {
                            if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
                                countDownLatch.countDown();
                            }
                        }
                    };
                    zooKeeper.exists(preNodeName, watcher);
                    if (millisToWait != null) {
                        millisToWait -= (System.currentTimeMillis() - startMillis);
                        startMillis = System.currentTimeMillis();
                        if (millisToWait <= 0) {
                            // timed out - delete our node
                            isDelete = true;
                            break;
                        }
                        countDownLatch.await(millisToWait, TimeUnit.MICROSECONDS);
                    } else {
                        countDownLatch.await();
                    }
                }

            }
        }catch (Exception e){
            deleteNode();
            isDelete = true;
            throw e;

        }finally {
            if (isDelete){
                deleteNode();
            }
        }
        return hasLock  ;
    }

    private Boolean attemptLock(long time, TimeUnit unit) throws Exception {
        final long startMillis = System.currentTimeMillis();
        final Long millisToWait = (unit != null) ? unit.toMillis(time) : null;

        boolean hasTheLock = false;
        boolean isDone = false;
        int retryCount = 0;

        // 网络闪断需要重试一试，最大重试次数MAX_RETRY_COUNT
        while (!isDone) {
            isDone = true;
            try {
                currentPath = createCurrentNode();
                hasTheLock = waitToLock(startMillis, millisToWait);

            } catch (Exception e) {
                if (retryCount++ < MAX_RETRY_COUNT) {
                    isDone = false;
                } else {
                    throw e;
                }
            }
        }

        return hasTheLock;
    }

    public static void main(String[] args) throws IOException {
        final ZooKeeper zooKeeper = new ZooKeeper("192.168.18.128:2181", 60000, null);
        DistributedLock myLock = new DistributedMuteLock(zooKeeper);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                public void run() {
                    DistributedLock myLock = new DistributedMuteLock(zooKeeper);
                    try {
                        myLock.lock();
                        Thread.sleep(1000);

                    } catch (Exception e) {

                    } finally {
                        myLock.unLock();
                    }
                }
            }).start();
        }

    }
}
