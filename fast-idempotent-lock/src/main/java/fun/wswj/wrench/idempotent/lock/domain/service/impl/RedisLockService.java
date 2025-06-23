package fun.wswj.wrench.idempotent.lock.domain.service.impl;


import fun.wswj.wrench.idempotent.lock.domain.service.ILockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class RedisLockService implements ILockService {

    private final RedissonClient redissonClient;

    public RedisLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Boolean tryLock(String lockName, long timeout, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockName);
        return lock.tryLock(timeout, timeUnit);
    }

    @Override
    public void freeLock(String lockName) {
        RLock lock = redissonClient.getLock(lockName);
        lock.unlock();
    }
}
