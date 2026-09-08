package com.qiujie.config.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 支持全局前缀的 String Key 序列化器。
 * <p>
 * 对写入 Redis 的所有 Key 统一附加指定前缀（如 "mall:"），
 * 读取/反序列化时自动剥离前缀，保证上层业务代码零侵入、零改动。
 */
public class PrefixStringRedisSerializer implements RedisSerializer<String> {

    private final String prefix;
    private final Charset charset;

    public PrefixStringRedisSerializer(String prefix) {
        this(prefix, StandardCharsets.UTF_8);
    }

    public PrefixStringRedisSerializer(String prefix, Charset charset) {
        this.prefix = StringUtils.hasText(prefix) ? prefix : "";
        this.charset = charset != null ? charset : StandardCharsets.UTF_8;
    }

    @Override
    public byte[] serialize(String string) throws SerializationException {
        if (string == null) {
            return null;
        }
        if (StringUtils.hasText(prefix) && !string.startsWith(prefix)) {
            string = prefix + string;
        }
        return string.getBytes(charset);
    }

    @Override
    public String deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }
        String string = new String(bytes, charset);
        if (StringUtils.hasText(prefix) && string.startsWith(prefix)) {
            return string.substring(prefix.length());
        }
        return string;
    }

    public String getPrefix() {
        return prefix;
    }
}
