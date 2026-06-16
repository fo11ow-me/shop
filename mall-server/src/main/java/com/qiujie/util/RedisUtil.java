package com.qiujie.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 底层操作封装 — KV、计数器、Lua 脚本、ZSet。
 *
 * @author sh.Liu
 */
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate redisTemplate;

    public boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    public long getTime(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // ==================== String ====================

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public Boolean setIfAbsent(String key, String value, long time) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public Long increment(String key, long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    // ==================== Lua ====================

    @SuppressWarnings("unchecked")
    public <T> T executeLua(String script, Class<T> returnType, List<String> keys, Object... argv) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(script);
        redisScript.setResultType(returnType);
        return (T) redisTemplate.execute(redisScript, keys, argv);
    }

    // ==================== ZSet ====================

    public void zIncrBy(String key, String member, double delta) {
        redisTemplate.opsForZSet().incrementScore(key, member, delta);
    }

    @SuppressWarnings("unchecked")
    public Set<ZSetOperations.TypedTuple<Object>> zRevRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }
}
