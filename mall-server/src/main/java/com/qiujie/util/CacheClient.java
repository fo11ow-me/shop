package com.qiujie.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存客户端接口 — 模板方法抽象，支持不同缓存策略热插拔
 *
 * @author qiujie
 */
public interface CacheClient {

    /**
     * 穿透保护：缓存未命中时查 DB，DB 返回 null 则写空值标记
     */
    <T, ID> T queryWithPassThrough(String keyPrefix, ID id, Class<T> clazz,
                                    Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit);

    /**
     * 击穿保护（互斥锁）：缓存未命中时加分布式锁，只有一个线程查 DB
     */
    <T, ID> T queryWithMutex(String keyPrefix, ID id, Class<T> clazz,
                              Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit);

    /**
     * 击穿保护（逻辑过期）：缓存已过期时返回旧值，异步后台重建
     */
    <T, ID> T queryWithLogicalExpire(String keyPrefix, ID id, Class<T> clazz,
                                      Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit);

    void set(String key, Object value, Long timeout, TimeUnit timeUnit);

    void setWithLogicalExpire(String key, Object value, Long timeout, TimeUnit timeUnit);

    void delete(String key);

    <T> T queryWithLogicalExpire(String fullKey, Class<T> clazz,
                                  java.util.function.Supplier<T> dbFallback, Long timeout, TimeUnit timeUnit);
}
