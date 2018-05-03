package com.zhongkk.zk.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author yechenglong
 * @create 2018/5/2 10:24
 **/
public interface DistributedLock {
    /**
     * 获取锁
     * @throws Exception
     */
    void lock() throws Exception;

    /**
     * 设置超时时间获取锁
     * @param time
     * @param timeUnit
     */
    void lock(Long time, TimeUnit timeUnit) throws Exception;

    /**
     * 释放锁
     * @throws Exception
     */
    void unLock();

    public boolean tryLock() throws Exception;
}
