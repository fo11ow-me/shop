package com.qiujie.util;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.qiujie.constants.RedisConstants.*;

@Component
public class CacheClient {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 缓存数据加入Redis，并设置过期时间
     *
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     */
    public void set(String key, Object value, Long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), timeout, timeUnit);
    }


    /**
     * 缓存数据加入Redis，并设置逻辑过期时间
     *
     * @param key
     * @param value
     * @param timeout
     * @param timeUnit
     */
    public void setWithLogicalExpire(String key, Object value, Long timeout, TimeUnit timeUnit) {
        RedisData redisData = new RedisData().setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(timeout)));
        this.set(key, redisData, timeout, timeUnit);
    }


    /**
     * 基于缓存空值解决缓存穿透问题
     *
     * @param keyPrefix
     * @param id
     * @param clazz
     * @param dbFallback
     * @param timeout
     * @param timeUnit
     * @param <T>
     * @param <ID>
     * @return
     */
    public <T, ID> T handleCachePenetration(String keyPrefix, ID id, Class<T> clazz, Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. 基于key查询缓存数据
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        T t = null;
        // 2. key是否命中，如果命中直接返回数据
        if (StrUtil.isNotBlank(jsonStr)) {
            t = JSONUtil.toBean(jsonStr, clazz);
            return t;
        }
        // 3. 判读是否为空字符串，如果为空字符串则返回null
        if (Objects.nonNull(jsonStr)) {
            return null;
        }
        // 4. 数据库查询数据
        t = dbFallback.apply(id);
        // 5. 数据不存在则返回null, redis缓存空值
        if (Objects.isNull(t)) {
            this.set(key, "", CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        // 6. 数据存在则返回数据，并写入缓存
        this.set(key, JSONUtil.toJsonStr(t), timeout, timeUnit);
        return t;
    }

    public static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <T, ID> T handleCacheBreakdown(String keyPrefix, ID id, Class<T> clazz, Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        // 1. 基于key查询缓存数据
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        // 2. 如果key未命中，直接返回null
        if (StrUtil.isBlank(jsonStr)) {
            return null;
        }
        // 3. 反序列化数据
        RedisData redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        JSONObject jsonObject = (JSONObject) redisData.getData();
        T t = JSONUtil.toBean(jsonObject, clazz);
        // 4. 如果key未过期，直接返回数据
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return t;
        }
        String lockKey = LOCK_PRODUCT_KEY + id;
        // 5. 获取锁
        if (tryLock(lockKey)) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T t1 = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, t1, timeout, timeUnit);
                } finally {
                    unLock(lockKey);
                }
            });
        }
        // 6. 再次检查缓存数据
        jsonStr = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isBlank(jsonStr)){
            return null;
        }
        redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        jsonObject = (JSONObject) redisData.getData();
        t = JSONUtil.toBean(jsonObject, clazz);
        if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
            return t;
        }
        return t;
    }


    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

}
