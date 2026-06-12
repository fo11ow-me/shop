package com.qiujie.service;

import com.qiujie.entity.User;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthServiceImpl tests")
class AuthServiceImplTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        redisTemplate.opsForValue().set("validate:code:test-uuid-001", "1234");
        User admin = userMapper.queryByCode("admin");
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
        }
    }

    @Test
    @DisplayName("register — creates new user with encoded password")
    void shouldRegisterNewUser() {
        authService.register("new_test_user", "test123", "13800138000", "1234", "test-uuid-001");

        var user = userMapper.queryByCode("new_test_user");
        assertNotNull(user);
        assertEquals("new_test_user", user.getCode());
        assertNotEquals("test123", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
    }

    @Test
    @DisplayName("register — throws on duplicate username")
    void shouldRejectDuplicateUsername() {
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            authService.register("admin", "test123", "13800138000", "1234", "test-uuid-001");
        });
        assertEquals(BusinessStatusEnum.USERNAME_EXISTS.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("register — throws on empty username")
    void shouldRejectEmptyUsername() {
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            authService.register("", "test123", "13800138000", "1234", "test-uuid-001");
        });
        assertEquals(BusinessStatusEnum.AUTH_EMPTY_CREDENTIALS.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("register — throws on empty password")
    void shouldRejectEmptyPassword() {
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            authService.register("test_user", "", "13800138000", "1234", "test-uuid-001");
        });
        assertEquals(BusinessStatusEnum.AUTH_EMPTY_CREDENTIALS.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login — returns token on correct credentials")
    void shouldLoginWithCorrectCredentials() {
        Map<String, Object> result = authService.login("admin", "admin123", "1234", "test-uuid-001", "/admin", null);

        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));
    }

    @Test
    @DisplayName("login — throws on wrong captcha")
    void shouldRejectWrongCaptcha() {
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            authService.login("admin", "admin123", "9999", "test-uuid-001", "/admin", null);
        });
        assertEquals(BusinessStatusEnum.CAPTCHA_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login — throws on missing captcha in Redis")
    void shouldRejectMissingCaptcha() {
        redisTemplate.delete("validate:code:test-uuid-001");
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            authService.login("admin", "admin123", "1234", "test-uuid-001", "/admin", null);
        });
        assertEquals(BusinessStatusEnum.CAPTCHA_NOT_EXIST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("login — throws on wrong password")
    void shouldRejectWrongPassword() {
        assertThrows(Exception.class, () -> {
            authService.login("admin", "wrong_password", "1234", "test-uuid-001", "/admin", null);
        });
    }
}
