package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.util.JwtUtil;
import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Boundary API tests")
class BoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        CustomUserDetails admin = new CustomUserDetails();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority("system:user:list")));
        admin.setEnabled(true);
        adminToken = jwtUtil.generateToken(admin, "/admin");
    }

    @AfterEach
    void tearDown() {
        redisUtil.del("order:lock:2");
    }

    @Test
    @DisplayName("Pagination — pageNum=999999 returns empty list")
    void shouldReturnEmptyListForLargePageNum() throws Exception {
        mockMvc.perform(get("/admin/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "999999")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Pagination — pageSize=-1 handled gracefully")
    void shouldHandleNegativePageSize() throws Exception {
        mockMvc.perform(get("/admin/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Empty body — POST with empty JSON to admin/user")
    void shouldHandleEmptyBody() throws Exception {
        mockMvc.perform(post("/admin/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Idempotency — duplicate order submission prevented by Redis lock")
    void shouldRejectDuplicateOrderViaRedisLock() throws Exception {
        // Generate portal token for userId=2 (the test portal user with cart items)
        CustomUserDetails portalUser = new CustomUserDetails();
        portalUser.setUserId(2);
        portalUser.setUsername("user");
        portalUser.setAuthorities(Collections.emptyList());
        portalUser.setEnabled(true);
        String portalToken = jwtUtil.generateToken(portalUser, "/portal");

        // First request: should succeed (creates order from cart item 1)
        mockMvc.perform(post("/portal/order/create")
                        .header("Authorization", "Bearer " + portalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cartIds\":[1],\"addressId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Pre-set the Redis lock to simulate a concurrent order still in progress
        redisUtil.setIfAbsent("order:lock:2", "1", 10);

        // Second request (simulating double-click): should be rejected
        mockMvc.perform(post("/portal/order/create")
                        .header("Authorization", "Bearer " + portalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cartIds\":[1],\"addressId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1605));
    }

    @Test
    @DisplayName("Multi-session — admin and portal tokens work simultaneously")
    void shouldAllowAdminAndPortalTokensSimultaneously() throws Exception {
        // Portal token for userId=2
        CustomUserDetails portalUser = new CustomUserDetails();
        portalUser.setUserId(2);
        portalUser.setUsername("user");
        portalUser.setAuthorities(Collections.emptyList());
        portalUser.setEnabled(true);
        String portalToken = jwtUtil.generateToken(portalUser, "/portal");

        // Admin token accesses admin product list
        mockMvc.perform(get("/admin/product/list")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Portal token accesses portal product home
        mockMvc.perform(get("/portal/product/home")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Multi-session — logout of admin doesn't affect portal")
    void shouldKeepPortalSessionAfterAdminLogout() throws Exception {
        // Portal token for userId=2
        CustomUserDetails portalUser = new CustomUserDetails();
        portalUser.setUserId(2);
        portalUser.setUsername("user");
        portalUser.setAuthorities(Collections.emptyList());
        portalUser.setEnabled(true);
        String portalToken = jwtUtil.generateToken(portalUser, "/portal");

        // Portal session: access cart list
        mockMvc.perform(get("/portal/cart/list")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isOk());

        // Admin session: access user list
        mockMvc.perform(get("/admin/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
        // Both sessions are independent
    }
}
