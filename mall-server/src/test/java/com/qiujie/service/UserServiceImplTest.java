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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserServiceImpl tests")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Re-encode seed user passwords so they match known credentials
        for (int id : new int[]{1, 2}) {
            User u = userMapper.selectById(id);
            if (u != null) {
                u.setPassword(passwordEncoder.encode("admin123"));
                userMapper.updateById(u);
            }
        }
    }

    @Test
    @DisplayName("add — creates user with encoded password")
    void shouldAddUser() {
        User user = new User();
        user.setCode("test_add");
        user.setName("Test User");
        user.setPassword("test123");

        userService.add(user);

        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
        assertNotNull(user.getPassword());
    }

    @Test
    @DisplayName("add — throws on empty code")
    void shouldThrowOnEmptyCode() {
        User user = new User();
        user.setCode("");
        user.setName("Test");
        user.setPassword("test");

        assertThrows(ServiceException.class, () -> userService.add(user));
    }

    @Test
    @DisplayName("add — throws on empty name")
    void shouldThrowOnEmptyName() {
        User user = new User();
        user.setCode("test");
        user.setName("");
        user.setPassword("test");

        assertThrows(ServiceException.class, () -> userService.add(user));
    }

    @Test
    @DisplayName("query — returns user by id")
    void shouldQueryUserById() {
        User user = userService.query(1);

        assertNotNull(user);
        assertEquals("admin", user.getCode());
    }

    @Test
    @DisplayName("query — throws on non-existent user")
    void shouldThrowOnMissingUser() {
        assertThrows(ServiceException.class, () -> userService.query(99999));
    }

    @Test
    @DisplayName("queryByCode — finds user by code")
    void shouldQueryUserByCode() {
        User user = userService.queryByCode("admin");

        assertNotNull(user);
        assertEquals(1, user.getId());
    }

    @Test
    @DisplayName("list — returns paginated users with filters")
    void shouldListUsers() {
        Map<String, Object> result = userService.list(1, 10, "", null, null, null, null, null, null, null);

        assertNotNull(result);
        assertTrue((Long) result.get("total") >= 2);
    }

    @Test
    @DisplayName("edit — updates user fields")
    void shouldEditUser() {
        User user = new User();
        user.setId(2);
        user.setName("Updated Name");

        userService.edit(user);

        User updated = userService.query(2);
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    @DisplayName("reset — encodes new password")
    void shouldResetPassword() {
        User user = new User();
        user.setId(2);
        user.setPassword("new_password");

        userService.reset(user);

        User updated = userService.query(2);
        assertTrue(updated.getPassword().startsWith("$2a$"));
        assertNotEquals("new_password", updated.getPassword());
    }

    @Test
    @DisplayName("validate — passes for correct password")
    void shouldValidateCorrectPassword() {
        assertDoesNotThrow(() -> userService.validate("admin123", 1));
    }

    @Test
    @DisplayName("validate — throws for wrong password")
    void shouldThrowOnWrongPassword() {
        assertThrows(ServiceException.class, () -> userService.validate("wrong", 1));
    }

    @Test
    @DisplayName("updateInfo — updates user profile fields")
    void shouldUpdateUserInfo() {
        Map<String, String> params = Map.of("name", "New Name", "phone", "13600000000");
        User result = userService.updateInfo(2, params);

        assertEquals("New Name", result.getName());
        assertEquals("13600000000", result.getPhone());
        assertNull(result.getPassword());
    }

    @Test
    @DisplayName("delete — removes user")
    void shouldDeleteUser() {
        userService.delete(2);

        assertThrows(ServiceException.class, () -> userService.query(2));
    }
}
