package fun.wswj.wrench.idempotent.lock.config;

import fun.wswj.wrench.dcc.config.DCCAutoProperties;
import fun.wswj.wrench.idempotent.lock.aop.IdempotentLockAop;
import fun.wswj.wrench.idempotent.lock.domain.service.ILockService;
import fun.wswj.wrench.idempotent.lock.domain.service.impl.LocalLockService;
import fun.wswj.wrench.idempotent.lock.domain.service.impl.RedisLockService;
import fun.wswj.wrench.idempotent.lock.types.enums.LockTypeEnum;
import fun.wswj.wrench.infrastructure.config.RedisClientConfigProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({IdempotentLockProperties.class,
                                RedisClientConfigProperties.class,
                                DCCAutoProperties.class})
public class IdempotentLockConfig {

    @Bean
    public ILockService lockService(IdempotentLockProperties idempotentLockProperties,
                                    RedissonClient redisClient){
        if(LockTypeEnum.REDIS.equals(idempotentLockProperties.getLockType())){
            return new RedisLockService(redisClient);
        }
        return new LocalLockService();
    }
    @Bean
    public IdempotentLockAop idempotentLockAspect(DCCAutoProperties dccAutoProperties,
                                                  IdempotentLockProperties idempotentLockProperties,
                                                  ILockService lockService) {

        return new IdempotentLockAop(dccAutoProperties, idempotentLockProperties, lockService);
    }

}
