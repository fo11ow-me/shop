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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Portal API tests")
class PortalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String portalToken;

    @BeforeEach
    void setUp() {
        CustomUserDetails portal = new CustomUserDetails();
        portal.setUserId(2);
        portal.setUsername("user");
        portal.setAuthorities(Collections.emptyList());
        portal.setEnabled(true);
        portalToken = jwtUtil.generateToken(portal, "/portal");
    }

    @Test
    @DisplayName("GET /portal/product/home — public, returns home data")
    void shouldReturnHomeData() throws Exception {
        mockMvc.perform(get("/portal/product/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /portal/product/detail/1 — returns product detail")
    void shouldReturnProductDetail() throws Exception {
        mockMvc.perform(get("/portal/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("iPhone 15"))
                .andExpect(jsonPath("$.data.price").isNumber());
    }

    @Test
    @DisplayName("GET /portal/product/search — returns search results")
    void shouldSearchProducts() throws Exception {
        mockMvc.perform(get("/portal/product/search")
                        .param("keyword", "iPhone")
                        .param("current", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /portal/product/categories — returns categories")
    void shouldReturnCategories() throws Exception {
        mockMvc.perform(get("/portal/product/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /portal/cart/list — authenticated, returns cart")
    void shouldReturnCartList() throws Exception {
        mockMvc.perform(get("/portal/cart/list")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /portal/order/list — authenticated, returns orders")
    void shouldReturnOrderList() throws Exception {
        mockMvc.perform(get("/portal/order/list")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("POST /portal/auth/register — creates user")
    void shouldRegisterUser() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "new_portal_user");
        req.put("password", "test123");
        req.put("phone", "13700000000");

        mockMvc.perform(post("/portal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("POST /portal/auth/register — rejects duplicate username")
    void shouldRejectDuplicateRegistration() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "user");
        req.put("password", "test123");
        req.put("phone", "13700000000");

        mockMvc.perform(post("/portal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1401));
    }
}
