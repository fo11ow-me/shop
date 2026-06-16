package com.qiujie.api;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Admin CRUD API tests")
class AdminCrudApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private String adminToken;

    @BeforeEach
    void setUp() {
        StpUtil.login(1);
        adminToken = StpUtil.getTokenValue();
    }

    // ======================== 商品管理 ========================

    @Test
    @DisplayName("PUT /admin/product — 更新商品名称")
    void shouldUpdateProductName() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("name", "更新测试商品");
        body.put("price", 199);
        body.put("categoryId", 1);
        body.put("stock", 50);

        mockMvc.perform(put("/admin/product")
                        .header("mall-token", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /admin/product/99999 — 逻辑删除不存在的商品也正常返回")
    void shouldDeleteProductGracefully() throws Exception {
        mockMvc.perform(delete("/admin/product/99999")
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ======================== 订单管理 ========================

    @Test
    @DisplayName("GET /admin/order/list — 分页查询订单")
    void shouldListAdminOrders() throws Exception {
        mockMvc.perform(get("/admin/order/list")
                        .header("mall-token", adminToken)
                        .param("current", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("GET /admin/order/detail/1 — 订单详情")
    void shouldGetOrderDetail() throws Exception {
        mockMvc.perform(get("/admin/order/detail/1")
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ======================== 仪表盘 ========================

    @Test
    @DisplayName("GET /admin/home/count — 仪表盘统计")
    void shouldReturnDashboardCount() throws Exception {
        mockMvc.perform(get("/admin/home/count")
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("GET /admin/enum/list — 枚举值列表")
    void shouldReturnEnumList() throws Exception {
        mockMvc.perform(get("/admin/enum/enums")
                        .header("mall-token", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap());
    }
}
