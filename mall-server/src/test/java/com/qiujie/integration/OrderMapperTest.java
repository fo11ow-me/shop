package com.qiujie.integration;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.entity.Order;
import com.qiujie.mapper.OrderMapper;

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
@DisplayName("OrderMapper integration tests")
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    @DisplayName("selectByUserId — returns orders with user name and items")
    void shouldSelectByUserId() {
        var orders = orderMapper.selectByUserId(2); // portal user

        assertNotNull(orders);
        assertTrue(orders.size() >= 1);
        assertNotNull(orders.get(0).getOrderSn());
        assertNotNull(orders.get(0).getItems());
    }

    @Test
    @DisplayName("selectDetailById — returns order with items")
    void shouldSelectDetailById() {
        var order = orderMapper.selectDetailById(1);

        assertNotNull(order);
        assertEquals("202605010001", order.getOrderSn());
        assertNotNull(order.getItems());
        assertTrue(order.getItems().size() >= 1);
        assertEquals("iPhone 15", order.getItems().get(0).getProductName());
    }

    @Test
    @DisplayName("selectPageWithParams — filters by orderSn")
    void shouldSelectPageWithOrderSn() {
        Page<Order> page = new Page<>(1, 10);
        var result = orderMapper.selectPageWithParams(page, "202605010001", null, null, null, null);

        assertEquals(1, result.getTotal());
        assertEquals("202605010001", result.getRecords().get(0).getOrderSn());
    }

    @Test
    @DisplayName("selectPageWithParams — filters by userName")
    void shouldSelectPageWithUserName() {
        Page<Order> page = new Page<>(1, 10);
        var result = orderMapper.selectPageWithParams(page, null, "测试", null, null, null);

        assertTrue(result.getTotal() >= 1);
    }

    @Test
    @DisplayName("selectPageWithParams — returns empty for unknown orderSn")
    void shouldReturnEmptyForUnknownOrderSn() {
        Page<Order> page = new Page<>(1, 10);
        var result = orderMapper.selectPageWithParams(page, "999999999999", null, null, null, null);

        assertEquals(0, result.getTotal());
    }

    // selectTrendData uses MySQL DATE_FORMAT — not testable with H2
    // selectCategorySales uses MySQL-specific syntax — not testable with H2
}
