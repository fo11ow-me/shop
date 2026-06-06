package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.entity.User;
import com.qiujie.mapper.UserMapper;
import com.qiujie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth API tests")
class AuthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String portalToken;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // Update admin user's password to known hash
        User admin = userMapper.queryByCode("admin");
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode("admin123"));
            userMapper.updateById(admin);
        }

        // Generate admin token programmatically
        CustomUserDetails adminDetails = new CustomUserDetails();
        adminDetails.setUserId(1);
        adminDetails.setUsername("admin");
        adminDetails.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority("system:user:list")));
        adminDetails.setEnabled(true);
        adminToken = jwtUtil.generateToken(adminDetails, "/admin");

        // Generate portal token programmatically
        CustomUserDetails portalDetails = new CustomUserDetails();
        portalDetails.setUserId(2);
        portalDetails.setUsername("user");
        portalDetails.setAuthorities(Collections.emptyList());
        portalDetails.setEnabled(true);
        portalToken = jwtUtil.generateToken(portalDetails, "/portal");
    }

    @Test
    @DisplayName("Portal token cannot access admin API — returns 401")
    void portalTokenCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/admin/user/list")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("Admin token can access portal public API — returns 200")
    void adminTokenCanAccessPortalPublicApi() throws Exception {
        mockMvc.perform(get("/portal/product/home")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin token cannot access portal authenticated API — returns 401")
    void adminTokenCannotAccessPortalAuthenticatedApi() throws Exception {
        mockMvc.perform(get("/portal/cart/list")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("No token — returns 401")
    void noTokenReturnsError() throws Exception {
        mockMvc.perform(get("/portal/cart/list"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("Forged token — returns 401")
    void forgedTokenReturnsError() throws Exception {
        mockMvc.perform(get("/portal/cart/list")
                        .header("Authorization", "Bearer this.is.not.valid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("Login with correct credentials returns 200 and token")
    void shouldLoginWithCorrectCredentials() throws Exception {
        redisTemplate.opsForValue().set("validate:code:test-uuid-001", "1234");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("code", "admin");
        loginRequest.put("password", "admin123");
        loginRequest.put("verificationCode", "1234");
        loginRequest.put("uuid", "test-uuid-001");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    @DisplayName("Login with wrong captcha — returns captcha error")
    void shouldRejectWrongCaptcha() throws Exception {
        redisTemplate.opsForValue().set("validate:code:test-uuid-001", "1234");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("code", "admin");
        loginRequest.put("password", "admin123");
        loginRequest.put("verificationCode", "9999");
        loginRequest.put("uuid", "test-uuid-001");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNumber());
    }
}
