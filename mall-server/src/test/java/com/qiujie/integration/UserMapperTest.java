package com.qiujie.integration;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.entity.User;
import com.qiujie.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserMapper integration tests")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("selectPage — returns paginated users")
    void shouldSelectPage() {
        Page<User> page = new Page<>(1, 10);
        Page<User> result = userMapper.selectPage(page, null);

        assertTrue(result.getTotal() >= 2);
        assertTrue(result.getRecords().size() >= 2);
    }

    @Test
    @DisplayName("queryByCode — finds user by unique code")
    void shouldQueryByCode() {
        User user = userMapper.queryByCode("admin");

        assertNotNull(user);
        assertEquals("admin", user.getCode());
        assertNotNull(user.getName());
    }

    @Test
    @DisplayName("queryByCode — returns null for non-existent code")
    void shouldReturnNullForMissingCode() {
        User user = userMapper.queryByCode("nonexistent_user");
        assertNull(user);
    }

    @Test
    @DisplayName("selectPage with status filter — returns only matching users")
    void shouldFilterByStatus() {
        Page<User> page = new Page<>(1, 10);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        Page<User> result = userMapper.selectPage(page, wrapper);

        assertTrue(result.getTotal() > 0);
        for (User u : result.getRecords()) {
            assertNotNull(u.getStatus());
        }
    }

    @Test
    @DisplayName("queryByCode — password field is mapped from pwd column")
    void shouldMapPasswordField() {
        User user = userMapper.queryByCode("admin");
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().startsWith("$2a$"));
    }
}
