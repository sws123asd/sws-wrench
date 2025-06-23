package fun.wswj.wrench.idempotent.lock.domain.service;


import java.util.concurrent.TimeUnit;

public interface ILockService {
    /**
     * 尝试获取锁
     *
     * @param lockName   锁名称
     * @param timeout    尝试时间
     * @param timeUnit   时间单位
     * @return true 获取锁成功，false 获取锁失败
     */
    Boolean tryLock(String lockName, long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 释放锁
     * @param lockName 锁名称
     */
    void freeLock(String lockName);
}
