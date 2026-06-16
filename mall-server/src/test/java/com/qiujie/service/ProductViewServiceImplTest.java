package com.qiujie.service;

import com.qiujie.entity.Product;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductViewService tests")
class ProductViewServiceImplTest {

    @Autowired
    private ProductViewService productViewService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void clearCache() {
        Set<String> keys = stringRedisTemplate.keys("cache:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("home — returns categories with products")
    void shouldReturnHomeData() {
        List<Map<String, Object>> result = productViewService.home();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("categories — returns tree structure")
    void shouldReturnCategoryTree() {
        var categories = productViewService.categories();
        assertNotNull(categories);
        assertTrue(categories.size() >= 2);
    }

    @Test
    @DisplayName("getByCategory — returns paginated products")
    void shouldGetProductsByCategory() {
        Map<String, Object> result = productViewService.getByCategory(2, 1, 10);
        assertNotNull(result);
        assertNotNull(result.get("records"));
    }

    @Test
    @DisplayName("detail — returns product")
    void shouldGetProductDetail() {
        Product product = productViewService.detail(1);
        assertNotNull(product);
        assertEquals(new BigDecimal("6999.00"), product.getPrice());
    }

    @Test
    @DisplayName("detail — throws for non-existent product")
    void shouldThrowOnMissingProduct() {
        assertThrows(ServiceException.class, () -> productViewService.detail(99999));
    }
}
