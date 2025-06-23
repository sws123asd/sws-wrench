package fun.wswj.wrench.idempotent.lock.context;

public class IdempotentLockContext {
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
