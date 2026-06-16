package com.qiujie.service;

import com.qiujie.document.ProductDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductSearchService tests")
class ProductSearchServiceImplTest {

    @Autowired
    private ProductSearchService productSearchService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("search — returns paginated results for keyword")
    void shouldSearchByKeyword() {
        Map<String, Object> result = productSearchService.search("手机", 1, 10);
        assertNotNull(result);
        assertNotNull(result.get("records"));
        assertNotNull(result.get("total"));
    }

    @Test
    @DisplayName("search — returns null for empty keyword")
    void shouldReturnNullForEmptyKeyword() {
        assertNull(productSearchService.search("", 1, 10));
        assertNull(productSearchService.search(null, 1, 10));
    }

    @Test
    @DisplayName("recommend — returns similar products excluding self")
    void shouldRecommendSimilarProducts() {
        List<ProductDocument> result = productSearchService.recommend(1, 8);
        assertNotNull(result);
        assertTrue(result.stream().noneMatch(doc -> doc.getId().equals(1)),
                "recommend should not include the source product");
    }

    @Test
    @DisplayName("recommend — returns empty for non-existent product")
    void shouldReturnEmptyForMissingProduct() {
        List<ProductDocument> result = productSearchService.recommend(99999, 8);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
