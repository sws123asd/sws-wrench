package fun.wswj.wrench.idempotent.lock.aop;

import fun.wswj.wrench.dcc.config.DCCAutoProperties;
import fun.wswj.wrench.idempotent.lock.config.IdempotentLockProperties;
import fun.wswj.wrench.idempotent.lock.context.IdempotentLockContext;
import fun.wswj.wrench.idempotent.lock.context.IdempotentLockContextThreadLocal;
import fun.wswj.wrench.idempotent.lock.domain.service.ILockService;
import fun.wswj.wrench.idempotent.lock.types.annotations.IdempotentLock;
import fun.wswj.wrench.idempotent.lock.types.exception.IdempotentException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


@Aspect
public class IdempotentLockAop {
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final String SEPARATOR = "-";

    private final DCCAutoProperties dccAutoProperties;
    private final IdempotentLockProperties idempotentLockProperties;
    private final ILockService lockService;

    public IdempotentLockAop(DCCAutoProperties dccAutoProperties, IdempotentLockProperties idempotentLockProperties, ILockService lockService) {
        this.dccAutoProperties = dccAutoProperties;
        this.idempotentLockProperties = idempotentLockProperties;
        this.lockService = lockService;
    }

    @Pointcut("@annotation(fun.wswj.wrench.idempotent.lock.types.annotations.IdempotentLock)")
    public void idempotentLockPointcut() {}
    @Around("idempotentLockPointcut()")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取连接点信息，转换为方法结构从而获取到方法上的注解信息
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        IdempotentLock idempotentLock = method.getAnnotation(IdempotentLock.class);
        String[] keys = idempotentLock.keys();
        if (keys.length == 0) {
            throw new IdempotentException("请指定锁的key");
        }
        Object[] args = joinPoint.getArgs();
        // 构建锁的key
        String key = assembleFullKey(method, args, keys);
        Boolean tryLock = lockService.tryLock(key, idempotentLock.tryTime(), TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(tryLock)) {
            Class<?> exceptionClass = idempotentLock.exception();
            throw (Throwable) exceptionClass.getConstructor(String.class).newInstance(idempotentLock.message());

        }
        // 生成并保存锁的上下文信息
        IdempotentLockContext context = new IdempotentLockContext();
        context.setKey(key);
        IdempotentLockContextThreadLocal.write(context);

        return joinPoint.proceed();
    }

    /**
     * 方法成功返回后执行操作
     */
    @AfterReturning(value = "idempotentLockPointcut()")
    public void afterReturning() {
        unlockAndClear();
    }

    /**
     * 方法抛出异常后执行操作
     */
    @AfterThrowing(value = "idempotentLockPointcut()")
    public void afterThrowing() {
        unlockAndClear();
    }

    private String assembleFullKey(Method method, Object[] args, String[] keys) {
        // 方法全名（包含返回值、类、方法名、参数类型等）
        String methodGenericString = method.toGenericString();
        // 去掉返回值信息，只取类、方法名、参数类型等
        // 提取从返回类型到参数列表结束的部分
        int paramEndIndex = methodGenericString.indexOf(")") + 1;
        if (paramEndIndex <= 0) {
            paramEndIndex = methodGenericString.length();
        }
        // 跳过 public/protected/private
        int returnTypeStart = methodGenericString.indexOf(" ") + 1;
        String methodSignaturePart = methodGenericString.substring(returnTypeStart, paramEndIndex).trim();

        String attrName = idempotentLockProperties.getKeyPrefix()
                + ":" + methodSignaturePart
                + ":" + calculateKey(method, args, keys);
        return dccAutoProperties.getKey(attrName);
    }

    private String calculateKey(Method method, Object[] args, String[] keys) {
        String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
        // 准备 Spring EL 表达式解析的上下文
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        if (null != parameterNames && parameterNames.length > 0) {
            for (int i = 0; i < parameterNames.length; i++) {
                evaluationContext.setVariable(parameterNames[i], args[i]);
            }
        }
        if (keys.length == 1) {
            return PARSER.parseExpression(keys[0]).getValue(evaluationContext, String.class);
        } else {
            StringJoiner sj = new StringJoiner(" + '" + SEPARATOR + "' + ");
            for (String key : keys) {
                sj.add(key);
            }
            Expression expression = PARSER.parseExpression(sj.toString());
            return expression.getValue(evaluationContext, String.class);
        }
    }

    private void unlockAndClear() {
        // 通过threadLocal 获取锁的上下文信息得到锁的key
        lockService.freeLock(IdempotentLockContextThreadLocal.read().getKey());
        // 清除threadLocal中的锁的上下文信息
        IdempotentLockContextThreadLocal.clear();
    }
}
