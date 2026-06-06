package com.qiujie.integration;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiujie.entity.Product;
import com.qiujie.enums.ProductStatusEnum;
import com.qiujie.mapper.ProductMapper;
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
@DisplayName("ProductMapper integration tests")
class ProductMapperTest {

    @Autowired
    private ProductMapper productMapper;

    @Test
    @DisplayName("search — finds products by keyword")
    void shouldSearchByKeyword() {
        List<Product> results = productMapper.search("iPhone");

        assertNotNull(results);
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(p -> p.getName().contains("iPhone")));
    }

    @Test
    @DisplayName("search — returns empty for unknown keyword")
    void shouldReturnEmptyForUnknownKeyword() {
        List<Product> results = productMapper.search("xyz_unknown_keyword_abc");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("selectByCategoryId — returns products in category and subcategories")
    void shouldSelectByCategoryId() {
        List<Product> results = productMapper.selectByCategoryId(1); // parent category

        assertNotNull(results);
        // Should include products from subcategory id=2 (iPhone) and direct (MacBook)
        assertTrue(results.size() >= 2);
    }

    @Test
    @DisplayName("selectPageByCategoryId — supports pagination")
    void shouldSelectPageByCategoryId() {
        Page<Product> page = new Page<>(1, 5);
        var result = productMapper.selectPageByCategoryId(page, 2);

        assertTrue(result.getTotal() >= 1);
        assertTrue(result.getRecords().size() >= 1);
    }

    @Test
    @DisplayName("selectPageByName — filters by name and status")
    void shouldSelectPageByName() {
        Page<Product> page = new Page<>(1, 10);
        var result = productMapper.selectPageByName(page, "Phone", 1, null);

        assertTrue(result.getTotal() >= 1);
        for (Product p : result.getRecords()) {
            assertEquals(ProductStatusEnum.ON_SHELF, p.getStatus());
        }
    }

    @Test
    @DisplayName("selectPageByName — filters by disabled status")
    void shouldFilterByDisabledStatus() {
        Page<Product> page = new Page<>(1, 10);
        var result = productMapper.selectPageByName(page, null, 0, null);

        assertTrue(result.getTotal() >= 1);
        for (Product p : result.getRecords()) {
            assertEquals(ProductStatusEnum.OFF_SHELF, p.getStatus());
        }
    }
}
