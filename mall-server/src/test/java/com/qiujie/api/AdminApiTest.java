package com.qiujie.api;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        StpUtil.login(1);
        adminToken = StpUtil.getTokenValue();
    }

    @Test
    @DisplayName("GET /admin/user — returns paginated users")
    void shouldListUsers() throws Exception {
        mockMvc.perform(get("/admin/user")
                        .header("mall-token", adminToken)
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
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /admin/product/list — returns paged products")
    void shouldListProducts() throws Exception {
        mockMvc.perform(get("/admin/product/list")
                        .header("mall-token", adminToken)
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
                        .header("mall-token", adminToken)
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
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}
