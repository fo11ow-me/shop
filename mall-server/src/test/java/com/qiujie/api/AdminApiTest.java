package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin CRUD API tests")
class AdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        CustomUserDetails admin = new CustomUserDetails();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        admin.setEnabled(true);
        adminToken = jwtUtil.generateToken(admin, "/admin");
    }

    @Test
    @DisplayName("GET /admin/user — returns paginated users")
    void shouldListUsers() throws Exception {
        mockMvc.perform(get("/admin/user")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.list").isArray());
    }

    @Test
    @DisplayName("GET /admin/category/tree — returns category tree")
    void shouldReturnCategoryTree() throws Exception {
        mockMvc.perform(get("/admin/category/tree")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /admin/product/list — returns paged products")
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/admin/product/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /admin/order/list — returns admin order list")
    void shouldListOrders() throws Exception {
        mockMvc.perform(get("/admin/order/list")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /admin/home/count — returns dashboard statistics")
    void shouldReturnDashboardStats() throws Exception {
        mockMvc.perform(get("/admin/home/count")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}
