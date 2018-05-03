package com.zhongkk.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author yechenglong
 * @create 2018/4/27 15:28
 **/
public class ZkTest {
    @Test
    public void testConnection() throws Exception{
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zk = new ZooKeeper("192.168.18.128:2181", 3000, new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        zk.create("/zk08","helloWorld".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        System.out.println("已经连接成功了");
        zk.create("/zk03","helloWorld".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
        while(true);
    }

    @Test
    public void testGet() throws Exception {
        ZooKeeper zk = ZkInstance.getZookeeper();
        final CountDownLatch cdl = new CountDownLatch(1);
        Stat stat = new Stat();
        System.out.println(String.valueOf(zk.getData("/zk03", new Watcher() {
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getType()== Event.EventType.NodeDataChanged){
                    System.out.println("数据变化了");
                    cdl.countDown();
                }

            }
        }, stat)));
//        System.out.println(stat.toString());
       // zk.setData("/zk03","hello1111".getBytes(),-1);
        cdl.await();
        System.out.println(String.valueOf(zk.getData("/zk03",null,stat)));

    }

    @Test
    public void test(){
        List<String> list = new ArrayList<String>();
        list.get(-1);
    }
}

class ZkInstance {
    private static ZooKeeper zk;
    private ZkInstance(){};
    public static ZooKeeper getZookeeper() throws Exception{
        if(zk==null){
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zk = new ZooKeeper("192.168.18.128:2181,192.168.18.127:2181,192.168.18.126:2181", 3000, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zk;
        }
        return zk;
    }
}
