package com.qiujie.service;

import com.qiujie.entity.Order;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.vo.OrderVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderServiceImpl tests")
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("createFromCart — creates order from selected cart items")
    void shouldCreateOrderFromCart() {
        Map<String, Object> params = new HashMap<>();
        params.put("addressId", 1);
        params.put("cartIds", List.of(1));
        params.put("payMethod", 1);

        Order order = orderService.createFromCart(2, params);

        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(OrderStatusEnum.PENDING_PAY, order.getStatus());
        assertTrue(order.getTotalAmount().doubleValue() > 0);
    }

    @Test
    @DisplayName("createFromCart — throws when addressId is missing")
    void shouldThrowOnMissingAddress() {
        Map<String, Object> params = new HashMap<>();
        params.put("addressId", 0);
        params.put("cartIds", List.of(1));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                orderService.createFromCart(2, params));
        assertEquals(BusinessStatusEnum.ADDRESS_NOT_EXIST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("list — returns user orders with items")
    void shouldListUserOrders() {
        List<OrderVO> orders = orderService.list(2);

        assertNotNull(orders);
        assertTrue(orders.size() >= 2);
        OrderVO first = orders.get(0);
        assertNotNull(first.getOrderSn());
    }

    @Test
    @DisplayName("pay — changes order status from PENDING_PAY to PAID")
    void shouldPayOrder() {
        orderService.pay(2, 1, 0);

        OrderVO order = orderService.detail(2, 1);
        assertEquals(OrderStatusEnum.PAID, order.getStatus());
    }

    @Test
    @DisplayName("pay — throws when order does not belong to user")
    void shouldThrowOnPayOtherUserOrder() {
        assertThrows(ServiceException.class, () -> orderService.pay(1, 1, 0));
    }

    @Test
    @DisplayName("cancel — changes status to CANCELLED for pending order")
    void shouldCancelPendingOrder() {
        orderService.cancel(1);

        OrderVO order = orderService.detail(2, 1);
        assertEquals(OrderStatusEnum.CANCELLED, order.getStatus());
    }

    @Test
    @DisplayName("cancel — throws when order is already paid")
    void shouldThrowOnCancelPaidOrder() {
        assertThrows(ServiceException.class, () -> orderService.cancel(2));
    }

    @Test
    @DisplayName("detail — returns full order with items")
    void shouldGetOrderDetail() {
        OrderVO order = orderService.detail(2, 1);

        assertNotNull(order);
        assertEquals(OrderStatusEnum.PENDING_PAY, order.getStatus());
        assertNotNull(order.getTotalAmount());
    }

    @Test
    @DisplayName("delete — removes order for user")
    void shouldDeleteOrder() {
        orderService.delete(2, 1);

        assertThrows(ServiceException.class, () -> orderService.detail(2, 1));
    }

    @Test
    @DisplayName("buyNow — creates order directly from product")
    void shouldBuyNow() {
        Map<String, Object> params = new HashMap<>();
        params.put("productId", 1);
        params.put("amount", 1);

        Order order = orderService.buyNow(2, params);

        assertNotNull(order);
        assertEquals(OrderStatusEnum.PENDING_PAY, order.getStatus());
    }
}
