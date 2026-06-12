package com.qiujie.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.qiujie.constants.RedisConstants.*;

/**
 * Redis 缓存客户端实现 — 穿透/击穿的三个模板方法
 *
 * @author qiujie
 */
@Component
public class RedisCacheClient implements CacheClient {

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private final StringRedisTemplate stringRedisTemplate;

    public RedisCacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void set(String key, Object value, Long timeout, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), timeout, timeUnit);
    }

    @Override
    public void setWithLogicalExpire(String key, Object value, Long timeout, TimeUnit timeUnit) {
        RedisData redisData = new RedisData().setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(timeout)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public <T, ID> T queryWithPassThrough(String keyPrefix, ID id, Class<T> clazz,
                                           Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toBean(jsonStr, clazz);
        }
        if (Objects.nonNull(jsonStr)) {
            return null;
        }
        T t = dbFallback.apply(id);
        if (Objects.isNull(t)) {
            this.set(key, "", CACHE_NULL_TTL, TimeUnit.SECONDS);
            return null;
        }
        this.set(key, JSONUtil.toJsonStr(t), timeout, timeUnit);
        return t;
    }

    @Override
    public <T, ID> T queryWithMutex(String keyPrefix, ID id, Class<T> clazz,
                                     Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(jsonStr)) {
            return JSONUtil.toBean(jsonStr, clazz);
        }
        String lockKey = LOCK_PRODUCT_KEY + id;
        try {
            while (!tryLock(lockKey)) {
                Thread.sleep(50);
                jsonStr = stringRedisTemplate.opsForValue().get(key);
                if (StrUtil.isNotBlank(jsonStr)) {
                    return JSONUtil.toBean(jsonStr, clazz);
                }
            }
            T t = dbFallback.apply(id);
            if (Objects.isNull(t)) {
                this.set(key, "", CACHE_NULL_TTL, TimeUnit.SECONDS);
                return null;
            }
            this.set(key, JSONUtil.toJsonStr(t), timeout, timeUnit);
            return t;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            unLock(lockKey);
        }
    }

    @Override
    public <T, ID> T queryWithLogicalExpire(String keyPrefix, ID id, Class<T> clazz,
                                             Function<ID, T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String key = keyPrefix + id;
        String jsonStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(jsonStr)) {
            T t = dbFallback.apply(id);
            if (t != null) {
                this.setWithLogicalExpire(key, t, timeout, timeUnit);
            }
            return t;
        }
        RedisData redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        T t = toBean(redisData.getData(), clazz);
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return t;
        }
        String lockKey = LOCK_PRODUCT_KEY + id;
        if (tryLock(lockKey)) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T t1 = dbFallback.apply(id);
                    if (t1 != null) {
                        this.setWithLogicalExpire(key, t1, timeout, timeUnit);
                    }
                } finally {
                    unLock(lockKey);
                }
            });
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private static <T> T toBean(Object data, Class<T> clazz) {
        if (data == null) return null;
        if (data instanceof JSONObject) {
            return JSONUtil.toBean((JSONObject) data, clazz);
        }
        return (T) JSONUtil.toBean(JSONUtil.toJsonStr(data), clazz);
    }

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public <T> T queryWithLogicalExpire(String fullKey, Class<T> clazz,
                                         java.util.function.Supplier<T> dbFallback, Long timeout, TimeUnit timeUnit) {
        String jsonStr = stringRedisTemplate.opsForValue().get(fullKey);
        if (StrUtil.isBlank(jsonStr)) {
            T t = dbFallback.get();
            if (t != null) {
                this.setWithLogicalExpire(fullKey, t, timeout, timeUnit);
            }
            return t;
        }
        RedisData redisData = JSONUtil.toBean(jsonStr, RedisData.class);
        T t = toBean(redisData.getData(), clazz);
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return t;
        }
        String lockKey = LOCK_PRODUCT_KEY + fullKey;
        if (tryLock(lockKey)) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T t1 = dbFallback.get();
                    if (t1 != null) {
                        this.setWithLogicalExpire(fullKey, t1, timeout, timeUnit);
                    }
                } finally {
                    unLock(lockKey);
                }
            });
        }
        return t;
    }
}
