package fun.wswj.wrench.infrastructure.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RedisClientConfigProperties.class)
public class RedisConfig {

    private final Logger log = LoggerFactory.getLogger(RedisConfig.class);
    @Bean(name = "redisClient")
    public RedissonClient redissonClient(RedisClientConfigProperties properties) {
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);

        // 单节点模式配置
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setConnectTimeout(properties.getConnectTimeout())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        // 设置密码（如果有）
        if (properties.getPassword() != null && !properties.getPassword().isEmpty()) {
            config.useSingleServer().setPassword(properties.getPassword());
        }
        RedissonClient redissonClient = Redisson.create(config);
        log.info("sws-wrench，注册器（redis）初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());
        return redissonClient;
    }
}