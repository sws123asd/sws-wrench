package fun.wswj.wrench.idempotent.lock.domain.service.impl;


import fun.wswj.wrench.idempotent.lock.domain.service.ILockService;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalLockService implements ILockService {
    Map<String, LockInfo> lockInfoMap = new ConcurrentHashMap<>(16);
    @Override
    public Boolean tryLock(String lockName, long timeout, TimeUnit timeUnit) throws InterruptedException {
        LockInfo lockInfo = lockInfoMap.get(lockName);
        if (Objects.isNull(lockInfo)) {
            LockInfo info = new LockInfo();
            info.setCount(0);
            info.setLock(new ReentrantLock());
            lockInfoMap.put(lockName, info);
        }
        lockInfo = lockInfoMap.get(lockName);
        boolean isLock = lockInfo.getLock().tryLock(timeout, timeUnit);
        lockInfo.setCount(lockInfo.getCount() + 1);
        return isLock;
    }

    @Override
    public void freeLock(String lockName) {
        LockInfo lockInfo = lockInfoMap.get(lockName);
        if(Objects.isNull(lockInfo)){
            return;
        }
        synchronized (lockInfo){
            int i = lockInfo.getCount() - 1;
            lockInfo.setCount(i);
            lockInfo.getLock().unlock();
            if(i <= 0){
                lockInfoMap.remove(lockName);
            }
        }
    }

    static class LockInfo {
        private Lock lock;
        private int count;

        public Lock getLock() {
            return lock;
        }

        public void setLock(Lock lock) {
            this.lock = lock;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
