package com.qiujie.integration;

import com.qiujie.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis serialization integration tests")
class RedisSerializationTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TEST_KEY = "test:serial:user";

    @AfterEach
    void tearDown() {
        redisTemplate.delete(TEST_KEY);
    }

    @Test
    @DisplayName("store and retrieve User object — fields preserved")
    void shouldSerializeAndDeserializeUser() {
        User user = new User();
        user.setId(100);
        user.setCode("test_user");
        user.setName("Test User");
        user.setPhone("13800138000");
        user.setEmail("test@example.com");
        user.setStatus(null); // will use enum default

        redisTemplate.opsForValue().set(TEST_KEY, user);

        User retrieved = (User) redisTemplate.opsForValue().get(TEST_KEY);
        assertNotNull(retrieved);
        assertEquals(100, retrieved.getId());
        assertEquals("test_user", retrieved.getCode());
        assertEquals("Test User", retrieved.getName());
        assertEquals("13800138000", retrieved.getPhone());
        assertEquals("test@example.com", retrieved.getEmail());
    }

    @Test
    @DisplayName("retrieve non-existent key — returns null")
    void shouldReturnNullForMissingKey() {
        Object result = redisTemplate.opsForValue().get("test:nonexistent:key:xyz");
        assertNull(result);
    }
}
