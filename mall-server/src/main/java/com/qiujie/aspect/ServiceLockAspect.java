package com.qiujie.aspect;

import com.qiujie.annotation.ServiceLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * ServiceLock AOP 切面 — 解析 SpEL → 获取锁 → 执行业务 → 释放锁
 *
 * @author qiujie
 */
@Aspect
@Component
public class ServiceLockAspect {

    private final RedissonClient redissonClient;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    public ServiceLockAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Around("@annotation(serviceLock)")
    public Object around(ProceedingJoinPoint jp, ServiceLock serviceLock) throws Throwable {
        String actualKey = parseKey(serviceLock.key(), jp);
        RLock lock = redissonClient.getLock(actualKey);
        boolean acquired = lock.tryLock(serviceLock.waitTime(), serviceLock.leaseTime(), TimeUnit.SECONDS);
        if (!acquired) {
            throw new RuntimeException("操作频繁，请稍后再试");
        }
        try {
            return jp.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String parseKey(String keyExpression, ProceedingJoinPoint jp) {
        if (!keyExpression.contains("#")) {
            return keyExpression;
        }
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = discoverer.getParameterNames(method);
        Object[] args = jp.getArgs();
        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
