package com.qiujie.integration;

import com.qiujie.entity.Cart;
import com.qiujie.mapper.CartMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CartMapper integration tests")
class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Test
    @DisplayName("selectByUserId — returns cart items with product info")
    void shouldSelectByUserId() {
        List<Cart> carts = cartMapper.selectByUserId(2);

        assertNotNull(carts);
        assertTrue(carts.size() >= 2);

        Cart first = carts.get(0);
        assertNotNull(first.getProductName());
        assertNotNull(first.getProductImg());
        assertTrue(first.getAmount() > 0);
    }

    @Test
    @DisplayName("selectByUserId — returns empty for user with no cart")
    void shouldReturnEmptyForNoCart() {
        List<Cart> carts = cartMapper.selectByUserId(99999);
        assertTrue(carts.isEmpty());
    }

    @Test
    @DisplayName("selectByUserIdAndProductId — finds existing cart item")
    void shouldFindByUserAndProduct() {
        var cart = cartMapper.selectByUserIdAndProductId(2, 1);

        assertNotNull(cart);
        assertEquals(2, cart.getAmount());
    }

    @Test
    @DisplayName("selectByUserIdAndProductId — returns null when not in cart")
    void shouldReturnNullForMissingCartItem() {
        var cart = cartMapper.selectByUserIdAndProductId(2, 99999);
        assertNull(cart);
    }
}
