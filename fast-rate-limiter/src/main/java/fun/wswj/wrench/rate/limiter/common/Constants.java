package fun.wswj.wrench.rate.limiter.common;

/**
 * 常量类
 * @author sws
 */
public class Constants {

    /**
     * Redis相关常量
     */
    public static class RedisKey {
        /**
         * 限流器前缀
         */
        public static final String RATE_LIMITER_PREFIX = "rate_limiter:";

        /**
         * 黑名单前缀
         */
        public static final String BLACKLIST_PREFIX = "rate_limiter_blacklist_count:";
    }

}
