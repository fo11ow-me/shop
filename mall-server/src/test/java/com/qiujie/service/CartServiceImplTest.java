package com.qiujie.service;

import com.qiujie.entity.Cart;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.vo.CartVO;
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
@DisplayName("CartServiceImpl tests")
class CartServiceImplTest {

    @Autowired
    private CartService cartService;

    @Test
    @DisplayName("list — returns cart items for user with product info")
    void shouldListCartItems() {
        List<CartVO> items = cartService.list(2);
        assertNotNull(items);
        assertTrue(items.size() >= 2);
        CartVO first = items.get(0);
        assertNotNull(first.getProductName());
        assertNotNull(first.getProductImg());
    }

    @Test
    @DisplayName("add — adds new cart item for product not in cart")
    void shouldAddNewCartItem() {
        Cart cart = new Cart();
        cart.setProductId(2);
        cart.setAmount(2);

        cartService.add(2, cart);

        List<CartVO> items = cartService.list(2);
        assertTrue(items.stream().anyMatch(i -> i.getProductId().equals(2) && i.getAmount() == 2));
    }

    @Test
    @DisplayName("add — increments amount when product already in cart")
    void shouldIncrementExistingCartItem() {
        Cart cart = new Cart();
        cart.setProductId(1);
        cart.setAmount(1);

        cartService.add(2, cart);

        List<CartVO> items = cartService.list(2);
        var existing = items.stream().filter(i -> i.getProductId().equals(1)).findFirst().orElseThrow();
        assertTrue(existing.getAmount() >= 3);
    }

    @Test
    @DisplayName("add — throws when product does not exist")
    void shouldThrowOnNonexistentProduct() {
        Cart cart = new Cart();
        cart.setProductId(99999);
        cart.setAmount(1);

        assertThrows(ServiceException.class, () -> cartService.add(2, cart));
    }

    @Test
    @DisplayName("add — defaults amount to 1 when null or zero")
    void shouldDefaultAmountToOne() {
        Cart cart = new Cart();
        cart.setProductId(2);
        cart.setAmount(null);

        cartService.add(2, cart);

        List<CartVO> items = cartService.list(2);
        var item = items.stream().filter(i -> i.getProductId().equals(2)).findFirst().orElseThrow();
        assertEquals(1, item.getAmount());
    }

    @Test
    @DisplayName("update — changes amount and isSelected")
    void shouldUpdateCartItem() {
        Cart cart = new Cart();
        cart.setId(1);
        cart.setAmount(5);
        cart.setIsSelected(0);

        cartService.update(2, cart);

        List<CartVO> items = cartService.list(2);
        var item = items.stream().filter(i -> i.getId().equals(1)).findFirst().orElseThrow();
        assertEquals(5, item.getAmount());
        assertEquals(0, item.getIsSelected());
    }

    @Test
    @DisplayName("update — throws when cart item belongs to other user")
    void shouldThrowOnOtherUserCartUpdate() {
        Cart cart = new Cart();
        cart.setId(1);
        cart.setAmount(10);

        assertThrows(ServiceException.class, () -> cartService.update(1, cart));
    }

    @Test
    @DisplayName("delete — removes cart item")
    void shouldDeleteCartItem() {
        cartService.delete(2, 1);

        List<CartVO> items = cartService.list(2);
        assertTrue(items.stream().noneMatch(i -> i.getId().equals(1)));
    }

    @Test
    @DisplayName("batchDelete — removes multiple cart items")
    void shouldBatchDeleteCartItems() {
        cartService.batchDelete(2, java.util.Map.of("ids", java.util.List.of(1, 2)));

        List<CartVO> items = cartService.list(2);
        assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("batchDelete — throws on empty id list")
    void shouldThrowOnEmptyBatchDelete() {
        assertThrows(ServiceException.class, () ->
                cartService.batchDelete(2, java.util.Map.of("ids", java.util.List.of())));
    }
}
