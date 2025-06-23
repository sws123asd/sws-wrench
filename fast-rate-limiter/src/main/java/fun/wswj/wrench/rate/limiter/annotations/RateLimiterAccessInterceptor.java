package fun.wswj.wrench.rate.limiter.annotations;

import java.lang.annotation.*;

/**
 * 限流注解
 * @author sws
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimiterAccessInterceptor {

    /**
     * 限流key
     */
    String key() default "all";

    /**
     * 允许的请求数
     */
    long permits() default 1L;

    /**
     * 时间窗口；单位：秒，默认1秒
     */
    long seconds() default 1L;

    /**
     * 存活时间；单位：秒，默认60秒
     */
    long timeToLive() default 3600L;

    /**
     * 黑名单计数，限流次数达到此值后将被加入黑名单 默认为0，表示无黑名单限制
     */
    int blacklistCount() default 0;

    /**
     * 降级方法名称
     */
    String fallbackMethod() default "";

}
