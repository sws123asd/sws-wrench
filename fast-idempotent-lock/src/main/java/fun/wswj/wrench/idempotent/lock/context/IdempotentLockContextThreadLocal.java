package fun.wswj.wrench.idempotent.lock.context;

import java.util.Objects;

public class IdempotentLockContextThreadLocal{

    private IdempotentLockContextThreadLocal(){}

    private static final ThreadLocal<IdempotentLockContext>
            DISTRIBUTION_LOCK_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 清除日志上下文信息
     */
    public static void clear() {
        DISTRIBUTION_LOCK_CONTEXT_THREAD_LOCAL.remove();
    }

    /**
     * 存储日志上下文信息
     */
    public static void write(IdempotentLockContext idempotentLockContext) {
        DISTRIBUTION_LOCK_CONTEXT_THREAD_LOCAL.set(idempotentLockContext);
    }

    /**
     * 获取当前日志上下文信息
     */
    public static IdempotentLockContext read() {
        IdempotentLockContext idempotentLockContext = DISTRIBUTION_LOCK_CONTEXT_THREAD_LOCAL.get();
        if (Objects.isNull(idempotentLockContext)) {
           idempotentLockContext = new IdempotentLockContext();
        }
        return idempotentLockContext;
    }
}
