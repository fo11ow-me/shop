package com.qiujie.service;

import com.qiujie.entity.Product;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.vo.ProductVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.qiujie.constants.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductServiceImpl tests")
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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
        List<Map<String, Object>> result = productService.home();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        Map<String, Object> first = result.get(0);
        assertNotNull(first.get("category"));
        assertNotNull(first.get("products"));
    }

    @Test
    @DisplayName("categories — returns tree structure with children")
    void shouldReturnCategoryTree() {
        var categories = productService.categories();

        assertNotNull(categories);
        assertTrue(categories.size() >= 2);
        var electronics = categories.stream()
                .filter(c -> c.getName().equals("电子产品")).findFirst().orElseThrow();
        assertNotNull(electronics.getChildren());
        assertFalse(electronics.getChildren().isEmpty());
    }

    @Test
    @DisplayName("getByCategory — returns paginated products for category")
    void shouldGetProductsByCategory() {
        Map<String, Object> result = productService.getByCategory(2, 1, 10);

        assertNotNull(result);
        assertNotNull(result.get("records"));
        assertNotNull(result.get("total"));
    }

    @Test
    @DisplayName("search — returns matching products by keyword")
    void shouldSearchProducts() {
        Map<String, Object> result = productService.search("iPhone", 1, 10);

        assertNotNull(result);
        var records = (List<?>) result.get("records");
        assertFalse(records.isEmpty());
    }

    @Test
    @DisplayName("search — returns null for empty keyword")
    void shouldReturnNullForEmptySearch() {
        Map<String, Object> result = productService.search("", 1, 10);
        assertNull(result);
    }

    @Test
    @DisplayName("detail — returns product with images")
    void shouldGetProductDetail() {
        ProductVO product = productService.detail(1);

        assertNotNull(product);
        assertEquals("iPhone 15", product.getName());
        assertEquals(new BigDecimal("6999.00"), product.getPrice());
    }

    @Test
    @DisplayName("detail — throws for non-existent product")
    void shouldThrowOnMissingProduct() {
        assertThrows(ServiceException.class, () -> productService.detail(99999));
    }

    @Test
    @DisplayName("add — creates product and returns id")
    void shouldAddProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("199.00"));
        product.setStock(50);

        productService.add(product);

        assertNotNull(product.getId());
        assertTrue(product.getId() > 0);
    }

    @Test
    @DisplayName("add — throws on negative price")
    void shouldThrowOnNegativePrice() {
        Product product = new Product();
        product.setName("Bad Product");
        product.setPrice(new BigDecimal("-10.00"));
        product.setStock(10);

        assertThrows(ServiceException.class, () -> productService.add(product));
    }

    @Test
    @DisplayName("add — throws on negative stock")
    void shouldThrowOnNegativeStock() {
        Product product = new Product();
        product.setName("Bad Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(-1);

        assertThrows(ServiceException.class, () -> productService.add(product));
    }

    @Test
    @DisplayName("toggleStatus — switches ON_SHELF to OFF_SHELF")
    void shouldToggleProductStatus() {
        productService.toggleStatus(1);

        ProductVO product = productService.detail(1);
        assertEquals(ProductStatusEnum.OFF_SHELF, product.getStatus());
    }

    @Test
    @DisplayName("listPage — returns paginated admin product list")
    void shouldListPageProducts() {
        var page = productService.listPage(1, 10, null, null, null);

        assertNotNull(page);
        assertTrue(page.getTotal() >= 3);
    }
}
