package fun.wswj.wrench.dcc.config;

import fun.wswj.wrench.dcc.domain.model.valobj.AttributeVO;
import fun.wswj.wrench.dcc.domain.service.RedisDCCService;
import fun.wswj.wrench.dcc.domain.service.IDCCService;
import fun.wswj.wrench.dcc.listener.DCCAdjustListener;
import fun.wswj.wrench.dcc.types.common.Constants;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册中心自动配置
 */
@Configuration
@EnableConfigurationProperties({DCCAutoProperties.class})
public class DCCRegisterAutoConfig {

    @Bean(name = "dccService")
    public IDCCService dynamicConfigCenterService(DCCAutoProperties dccAutoProperties,
                                                  RedissonClient redisClient) {
        return new RedisDCCService(dccAutoProperties, redisClient);
    }

    @Bean
    public DCCAdjustListener dccAdjustListener(IDCCService dccService) {
        return new DCCAdjustListener(dccService);
    }

    @Bean(name = "dccRedisTopic")
    public RTopic threadPoolConfigAdjustListener(DCCAutoProperties dccAutoProperties,
                                                 RedissonClient redisClient,
                                                 DCCAdjustListener dccAdjustListener) {
        RTopic topic = redisClient.getTopic(Constants.getTopic(dccAutoProperties.getSystem()));
        topic.addListener(AttributeVO.class, dccAdjustListener);
        return topic;
    }
}
