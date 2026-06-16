package com.qiujie.service;

import com.qiujie.entity.Product;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductAdminService tests")
class ProductAdminServiceImplTest {

    @Autowired
    private ProductAdminService productAdminService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("add — creates product")
    void shouldAddProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("199.00"));
        product.setStock(50);
        product.setCategoryId(1);

        productAdminService.add(product);
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

        assertThrows(ServiceException.class, () -> productAdminService.add(product));
    }

    @Test
    @DisplayName("add — throws on negative stock")
    void shouldThrowOnNegativeStock() {
        Product product = new Product();
        product.setName("Bad Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(-1);

        assertThrows(ServiceException.class, () -> productAdminService.add(product));
    }

    @Test
    @DisplayName("listPage — returns paginated admin product list")
    void shouldListPageProducts() {
        var page = productAdminService.listPage(1, 10, null, null, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 3);
    }
}
