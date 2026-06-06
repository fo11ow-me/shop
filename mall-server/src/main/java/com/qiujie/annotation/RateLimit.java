package com.qiujie.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * <p>
 * 配合 {@code RateLimitInterceptor} 实现基于 Redis 的滑动窗口限流，
 * 通过 INCR + EXPIRE 在指定时间窗口内限制同一客户端 IP 的请求次数。
 * </p>
 *
 * @author qiujie
 * @date 2026/05/30
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Redis key 前缀，最终 key 为 {@code prefix + 客户端IP}
     */
    String key();

    /**
     * 时间窗口内允许的最大请求次数
     */
    int limit() default 5;

    /**
     * 时间窗口大小（秒）
     */
    int window() default 60;

    /**
     * 触发限流时返回的错误提示
     */
    String message() default "请求过于频繁，请稍后再试";
}
