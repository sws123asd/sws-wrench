package fun.wswj.wrench.dcc.domain.service;

import fun.wswj.wrench.dcc.config.DCCAutoProperties;
import fun.wswj.wrench.dcc.domain.model.valobj.AttributeVO;
import fun.wswj.wrench.dcc.types.annotations.DCCValue;
import fun.wswj.wrench.dcc.types.common.Constants;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态配置中心服务实现类
 * @author sws
 */
public class RedisDCCService implements IDCCService {
    private final Logger log = LoggerFactory.getLogger(RedisDCCService.class);

    private final DCCAutoProperties properties;

    private final RedissonClient redissonClient;

    private final Map<String, Object> dccBeanGroup = new ConcurrentHashMap<>();

    public RedisDCCService(DCCAutoProperties properties, RedissonClient redissonClient) {
        this.properties = properties;
        this.redissonClient = redissonClient;
    }
    @Override
    public Object proxyObject(Object bean) {
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject = bean;
        if(AopUtils.isAopProxy(bean)){
            targetBeanClass = AopUtils.getTargetClass(bean);
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean);
        }
        // 拿到添加DCCValue注解的属性字段
        Field[] fields = targetBeanClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }
            // 切分字符串
            String[] splits = value.split(Constants.SYMBOL_COLON);
            // 构建缓存key
            String key = properties.getKey(splits[0].trim());

            String defaultValue = splits.length == 2 ? splits[1] : null;
            // 设置值
            String setValue = defaultValue;
            try {
                if (StringUtils.isBlank(defaultValue)) {
                    throw new RuntimeException("dcc config error " + key + " is not null - 请配置默认值！");
                }
                // Redis 操作，判断配置Key是否存在，不存在则创建，存在则获取最新值
                RBucket<String> bucket = redissonClient.getBucket(key);
                boolean exists = bucket.isExists();
                if (!exists) {
                    bucket.set(defaultValue);
                } else {
                    setValue = bucket.get();
                }
                // 填充属性字段
                field.setAccessible(true);
                field.set(targetBeanObject, setValue);
                field.setAccessible(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            dccBeanGroup.put(key, targetBeanObject);
        }
        return bean;
    }

    /**
     * 更新配置值
     */
    @Override
    public String updateConfig(AttributeVO attributeVO) {
        if(StringUtils.isBlank(attributeVO.getAttribute()) || StringUtils.isBlank(attributeVO.getValue())){
            return "请正确填写 attribute 和 value";
        }
        // 属性信息
        String key = properties.getKey(attributeVO.getAttribute());
        String value = attributeVO.getValue();

        // 设置值
        RBucket<String> bucket = redissonClient.getBucket(key);
        boolean exists = bucket.isExists();
        if (!exists) return key + "配置不存在";
        bucket.set(attributeVO.getValue());

        Object objBean = dccBeanGroup.get(key);
        if (null == objBean) return "对象不存在";

        Class<?> objBeanClass = objBean.getClass();
        // 检查 objBean 是否是代理对象
        if (AopUtils.isAopProxy(objBean)) {
            // 获取代理对象的目标对象
            objBeanClass = AopUtils.getTargetClass(objBean);
        }

        try {
            // 1. getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
            // 2. getField 方法用于获取指定类中的公共字段，即只能获取到公共访问修饰符（public）的字段。
            Field field = objBeanClass.getDeclaredField(attributeVO.getAttribute());
            field.setAccessible(true);
            field.set(objBean, value);
            field.setAccessible(false);

            log.info("DCC 节点监听，动态设置值 {} {}", key, value);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "更新成功";
    }
}
