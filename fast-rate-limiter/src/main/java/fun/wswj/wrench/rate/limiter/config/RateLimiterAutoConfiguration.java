package fun.wswj.wrench.rate.limiter.config;

import fun.wswj.wrench.infrastructure.config.RedisClientConfigProperties;
import fun.wswj.wrench.rate.limiter.aop.RateLimiterAOP;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 * @author sws
 */
@Configuration
@EnableConfigurationProperties(RedisClientConfigProperties.class)
public class RateLimiterAutoConfiguration {

    @Bean
    public RateLimiterAOP rateLimiterAOP(RedissonClient redisClient) {
        return new RateLimiterAOP(redisClient);
    }
}
