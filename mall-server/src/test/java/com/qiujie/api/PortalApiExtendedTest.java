package com.qiujie.api;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Portal auth & cart API tests")
class PortalApiExtendedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private String portalToken;

    @BeforeEach
    void setUp() {
        StpUtil.login(2);
        portalToken = StpUtil.getTokenValue();
    }

    // ======================== 认证 ========================

    @Test
    @DisplayName("POST /portal/auth/login — 缺少验证码返回 1404")
    void shouldRequireCaptchaForLogin() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "user");
        req.put("password", "123");

        mockMvc.perform(post("/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1404));
    }

    @Test
    @DisplayName("POST /portal/auth/login — 验证码错误返回 1404")
    void shouldRejectWrongCaptcha() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "user");
        req.put("password", "123");
        req.put("verificationCode", "0000");
        req.put("uuid", "fake-uuid");

        mockMvc.perform(post("/portal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1404));
    }

    // ======================== 购物车 ========================

    @Test
    @DisplayName("GET /portal/cart/list — 认证后返回购物车")
    void shouldReturnCartList() throws Exception {
        mockMvc.perform(get("/portal/cart/list")
                        .header("mall-token", portalToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /portal/cart/list — 未认证返回 401")
    void shouldRejectUnauthenticatedCart() throws Exception {
        mockMvc.perform(get("/portal/cart/list"))
                .andExpect(status().isUnauthorized());
    }

    // ======================== 商品浏览 ========================

    @Test
    @DisplayName("GET /portal/product/home — 首页数据")
    void shouldReturnHomeData() throws Exception {
        mockMvc.perform(get("/portal/product/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /portal/product/detail/1 — 商品详情")
    void shouldReturnProductDetail() throws Exception {
        mockMvc.perform(get("/portal/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").isString())
                .andExpect(jsonPath("$.data.price").isNumber());
    }

    @Test
    @DisplayName("GET /portal/product/categories — 分类列表")
    void shouldReturnCategories() throws Exception {
        mockMvc.perform(get("/portal/product/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
