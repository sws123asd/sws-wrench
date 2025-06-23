package fun.wswj.wrench.dcc.types.annotations;

import java.lang.annotation.*;

/**
 * 注解，动态配置中心
 * @author sws
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {

    /**
     * 配置格式：key 或 key:defaultValue
     * 例如 rateLimiterSwitch:close
     */
    String value() default "";

}
