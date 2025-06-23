package fun.wswj.wrench.rate.limiter.aop;

import fun.wswj.wrench.dcc.types.annotations.DCCValue;
import fun.wswj.wrench.rate.limiter.annotations.RateLimiterAccessInterceptor;
import fun.wswj.wrench.rate.limiter.common.Constants;
import fun.wswj.wrench.rate.limiter.utils.AttrUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 限流切面
 * @author sws
 */
@Aspect
public class RateLimiterAOP {
    private final Logger log = LoggerFactory.getLogger(RateLimiterAOP.class);
    /**
     * 限流开关，默认开启
     */
    @DCCValue("rateLimiterSwitch:open")
    private String rateLimiterSwitch;

    private final RedissonClient redissonClient;

    public RateLimiterAOP(RedissonClient redissonClient) {

        this.redissonClient = redissonClient;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(fun.wswj.wrench.rate.limiter.annotations.RateLimiterAccessInterceptor)")
    public void rateLimiterPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("rateLimiterPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 如果限流开关关闭，直接放行
        if ("close".equalsIgnoreCase(rateLimiterSwitch)) {
            return joinPoint.proceed();
        }

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解
        RateLimiterAccessInterceptor annotation = method.getAnnotation(RateLimiterAccessInterceptor.class);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        // 获取限流key
        String key = annotation.key();

        if (!StringUtils.hasText(key)) {
            throw new RuntimeException("annotation RateLimiter key is null！");
        }
        String attrValue = AttrUtils.getAttrValue(key, joinPoint.getArgs());

        // 检查是否构建黑名单
        String blacklistKey = Constants.RedisKey.BLACKLIST_PREFIX + attrValue;
        RAtomicLong atomicLong = redissonClient.getAtomicLong(blacklistKey);
        if(annotation.blacklistCount() > 0 && !"all".equals(attrValue)){
            boolean exists = atomicLong.isExists();
            long atomicCount = atomicLong.get();
            if(exists && atomicCount >= annotation.blacklistCount()){
                log.info("redis [{}] 限流 黑名单拦截(24h): {}",attrValue, blacklistKey);
                return handleFallback(joinPoint, annotation.fallbackMethod());
            }else if(!exists){
                atomicLong.expire(Duration.ofHours(24));
            }
        }

        // 获取限流器
        String rateLimiterKey = Constants.RedisKey.RATE_LIMITER_PREFIX + attrValue;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

        boolean acquired = false;
        try {
            // 初始化限流器（如果需要）
            if (!rateLimiter.isExists()) {
                rateLimiter.trySetRate(RateType.PER_CLIENT, annotation.permits(), Duration.ofSeconds(annotation.seconds()),Duration.ofSeconds(annotation.timeToLive()));
            }

            // 尝试获取令牌
            acquired = rateLimiter.tryAcquire();

            if(!acquired){
                // 获取令牌失败，进行黑名单计数
                if (annotation.blacklistCount() > 0) {
                    long count = atomicLong.incrementAndGet();
                    // 如果达到黑名单阈值，加入黑名单
                    if (count >= annotation.blacklistCount()) {
                        log.warn("请求达到黑名单阈值，加入黑名单，key: {}, count: {}", attrValue, count);
                    }
                }
                // 调用降级方法
                return handleFallback(joinPoint, annotation.fallbackMethod());
            }
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error("限流异常: {}", e.getMessage(), e);
            // 发生异常时，如果已获取令牌，则执行原方法，否则调用降级方法
            return acquired ? joinPoint.proceed() : handleFallback(joinPoint, annotation.fallbackMethod());
        }
    }

    /**
     * 处理降级方法
     */
    private Object handleFallback(ProceedingJoinPoint joinPoint, String fallbackMethod) throws Throwable {
        if (StringUtils.hasText(fallbackMethod)) {
            try {
                // 获取目标对象
                Object target = joinPoint.getTarget();
                // 获取参数类型
                Class<?>[] parameterTypes = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes();
                // 获取降级方法
                Method method = target.getClass().getDeclaredMethod(fallbackMethod, parameterTypes);
                method.setAccessible(true);
                // 调用降级方法
                return method.invoke(target, joinPoint.getArgs());
            } catch (Exception e) {
                log.error("调用降级方法异常: {}", e.getMessage(), e);
            }
        }
        // 默认返回限流响应
        return joinPoint.proceed();
    }

}
