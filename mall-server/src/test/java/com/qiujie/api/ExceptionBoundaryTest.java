package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.util.JwtUtil;
import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Exception boundary tests")
class ExceptionBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    @AfterEach
    void tearDown() {
        redisUtil.del("validate:code:test-uuid");
        redisUtil.del("order:lock:2");
    }

    @Test
    @DisplayName("Captcha not available — login fails with CAPTCHA_NOT_EXIST (1404)")
    void captchaNotExistShouldRejectLogin() throws Exception {
        // Ensure no captcha key exists in Redis
        redisUtil.del("validate:code:test-uuid");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("code", "admin");
        loginRequest.put("password", "admin123");
        loginRequest.put("verificationCode", "1234");
        loginRequest.put("uuid", "test-uuid");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1404))
                .andExpect(jsonPath("$.message").value("验证码不存在"));
    }

    @Test
    @DisplayName("Captcha value mismatch — login fails with CAPTCHA_ERROR (1406)")
    void captchaMismatchShouldRejectLogin() throws Exception {
        // Set a captcha in Redis
        redisUtil.set("validate:code:test-uuid", "5678");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("code", "admin");
        loginRequest.put("password", "admin123");
        loginRequest.put("verificationCode", "1234");
        loginRequest.put("uuid", "test-uuid");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1406))
                .andExpect(jsonPath("$.message").value("验证码错误"));
    }

    @Test
    @DisplayName("Order lock held — order creation fails with ORDER_IN_PROGRESS (1605)")
    void orderLockHeldShouldRejectOrder() throws Exception {
        // Pre-set the Redis lock to simulate a concurrent order in progress for userId=2
        redisUtil.setIfAbsent("order:lock:2", "1", 10);

        CustomUserDetails portalUser = new CustomUserDetails();
        portalUser.setUserId(2);
        portalUser.setUsername("user");
        portalUser.setAuthorities(Collections.emptyList());
        portalUser.setEnabled(true);
        String portalToken = jwtUtil.generateToken(portalUser, "/portal");

        mockMvc.perform(post("/portal/order/create")
                        .header("Authorization", "Bearer " + portalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cartIds\":[1],\"addressId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1605))
                .andExpect(jsonPath("$.message").value("您的订单正在处理中，请勿重复提交"));
    }

    @Test
    @DisplayName("Redis connection configured — context loads successfully")
    void contextLoadsWithRedisConfig() {
        // If this test runs, the @SpringBootTest context loaded successfully
        // with Redis connection details from application-test.yml
        assert redisUtil != null : "RedisUtil should be injected";
    }

    @Test
    @DisplayName("Empty captcha code — login fails with CAPTCHA_NOT_EXIST (1404)")
    void emptyCaptchaShouldRejectLogin() throws Exception {
        // Set captcha to null/empty state by not setting it
        redisUtil.del("validate:code:test-uuid");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("code", "admin");
        loginRequest.put("password", "admin123");
        loginRequest.put("verificationCode", "");
        loginRequest.put("uuid", "test-uuid");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1404));
    }
}
