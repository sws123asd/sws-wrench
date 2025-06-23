package fun.wswj.wrench.dcc.domain.service;

import fun.wswj.wrench.dcc.domain.model.valobj.AttributeVO;

/**
 * 动态配置中心服务接口
 * @author sws
 */
public interface IDCCService {

    /**
     * 初始化设置配置属性值和缓存
     * @param bean 实例对象
     * @return bean
     */
    Object proxyObject(Object bean);

    /**
     * 更新配置值
     * @return 响应结果
     */
    String updateConfig(AttributeVO attributeVO);
}
