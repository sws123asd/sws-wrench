package fun.wswj.wrench.idempotent.lock.types.annotations;


import fun.wswj.wrench.idempotent.lock.types.exception.IdempotentException;

import java.lang.annotation.*;

/**
 * @author sws
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface IdempotentLock {
    /**
     * 标注参数中用于生成锁的key的字段。（支持SpEL）
     * <p>如果参数是对象，写法：#对象名.字段名，例如：#user.userName</p>
     * <p>如果参数不是对象，写法：#字段名。例如：#orderNo</p>
     */
    String[] keys();

    /**
     * 获取锁的尝试时间。单位：秒
     */
    long tryTime() default 0L;

    /**
     * 错误提示。若定义了exception，则作为其message。
     */
    String message() default "正在处理，请稍候重试";

    /**
     * 报错时抛出的异常
     */
    Class<? extends Exception> exception() default IdempotentException.class;
}
