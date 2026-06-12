package com.qiujie.annotation;

import java.lang.annotation.*;

/**
 * 声明式分布式锁 — 方法执行前自动获取 Redisson 锁，执行后释放
 *
 * @author qiujie
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLock {

    /** 锁 key，支持 SpEL 表达式，如 "lock:order:#{#userId}" */
    String key();

    /** 获取锁等待时间（秒），默认 1 秒，超时抛异常 */
    long waitTime() default 1;

    /** 锁持有时间（秒），默认 5 秒后自动释放 */
    long leaseTime() default 5;
}
