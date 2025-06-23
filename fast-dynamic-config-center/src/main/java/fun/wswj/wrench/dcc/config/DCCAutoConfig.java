package fun.wswj.wrench.dcc.config;

import fun.wswj.wrench.dcc.domain.service.IDCCService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类 在实例化之后进行构建动态值
 */
@Configuration
public class DCCAutoConfig implements BeanPostProcessor {

    private final IDCCService dccService;

    public DCCAutoConfig(IDCCService dccService) {
        this.dccService = dccService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return dccService.proxyObject(bean);
    }
}
