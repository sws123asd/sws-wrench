package fun.wswj.wrench.idempotent.lock.config;


import fun.wswj.wrench.idempotent.lock.types.enums.LockTypeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wswj.wrench.config.idempotent.lock", ignoreInvalidFields = true)
public class IdempotentLockProperties {
    /**
     * 存储方式
     */
    private LockTypeEnum lockType = LockTypeEnum.LOCAL;

    /**
     * 存到Redis里的key的前缀
     */
    private String keyPrefix = "idempotent-lock";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public LockTypeEnum getLockType() {
        return lockType;
    }

    public void setLockType(LockTypeEnum lockType) {
        this.lockType = lockType;
    }
}
