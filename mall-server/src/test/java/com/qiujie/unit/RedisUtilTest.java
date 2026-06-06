package com.qiujie.unit;

import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RedisUtil integration tests (real Redis db1)")
class RedisUtilTest {

    @Autowired
    private RedisUtil redisUtil;

    private static final String TEST_KEY = "test:unit:key";

    @AfterEach
    void tearDown() {
        redisUtil.del(TEST_KEY);
    }

    @Test
    @DisplayName("set then get — returns stored value")
    void shouldSetAndGet() {
        redisUtil.set(TEST_KEY, "hello");

        Object result = redisUtil.get(TEST_KEY);
        assertEquals("hello", result);
    }

    @Test
    @DisplayName("get non-existent key — returns null")
    void shouldReturnNullForMissingKey() {
        Object result = redisUtil.get("test:nonexistent:key:xyz");
        assertNull(result);
    }

    @Test
    @DisplayName("set with TTL — value expires after TTL")
    void shouldExpireAfterTtl() throws InterruptedException {
        redisUtil.set(TEST_KEY, "ephemeral", 2L);

        // immediately should exist
        assertEquals("ephemeral", redisUtil.get(TEST_KEY));

        // wait for expiration
        Thread.sleep(2500);

        assertNull(redisUtil.get(TEST_KEY));
    }

    @Test
    @DisplayName("del — removes key so subsequent get returns null")
    void shouldDeleteKey() {
        redisUtil.set(TEST_KEY, "to-delete");
        redisUtil.del(TEST_KEY);

        assertNull(redisUtil.get(TEST_KEY));
    }

    @Test
    @DisplayName("get null key — returns null")
    void shouldReturnNullForNullKey() {
        assertNull(redisUtil.get(null));
    }

    @Test
    @DisplayName("expire — sets TTL on existing key")
    void shouldExpireExistingKey() throws InterruptedException {
        redisUtil.set(TEST_KEY, "will-expire");
        assertTrue(redisUtil.expire(TEST_KEY, 1L));

        // key should exist before TTL
        assertEquals("will-expire", redisUtil.get(TEST_KEY));

        Thread.sleep(1500);
        assertNull(redisUtil.get(TEST_KEY));
    }

    @Test
    @DisplayName("hasKey — returns true for existing, false after del")
    void shouldCheckKeyExistence() {
        redisUtil.set(TEST_KEY, "exists");
        assertTrue(redisUtil.hasKey(TEST_KEY));

        redisUtil.del(TEST_KEY);
        assertFalse(redisUtil.hasKey(TEST_KEY));
    }
}
