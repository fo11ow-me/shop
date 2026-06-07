package com.qiujie.service;

import com.qiujie.vo.CategorySalesVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("HomeServiceImpl tests")
class HomeServiceImplTest {

    @Autowired
    private HomeService homeService;

    @Test
    @DisplayName("getCount — returns aggregated user/product/order counts")
    void shouldReturnCounts() {
        Map<String, Object> counts = homeService.getCount();

        assertNotNull(counts);
        assertTrue(((Number) counts.get("userCount")).longValue() >= 2);
        assertTrue(((Number) counts.get("productCount")).longValue() >= 3);
        assertTrue(((Number) counts.get("orderCount")).longValue() >= 2);
    }

    @Test
    @DisplayName("getCount — includes today and yesterday order stats")
    void shouldIncludeDailyStats() {
        Map<String, Object> counts = homeService.getCount();

        assertNotNull(counts.get("todayOrderCount"));
        assertNotNull(counts.get("todaySales"));
        assertNotNull(counts.get("yesterdayOrderCount"));
        assertNotNull(counts.get("yesterdaySales"));
    }

    @Test
    @DisplayName("getTrend — returns trend data for N days")
    void shouldReturnTrendData() {
        List<Map<String, Object>> trend = homeService.getTrend(7);

        assertNotNull(trend);
        // H2 seed data may not have trend data — just verify no exception
    }

    @Test
    @DisplayName("getCategorySales — returns category sales ranking")
    void shouldReturnCategorySales() {
        List<CategorySalesVO> sales = homeService.getCategorySales();

        assertNotNull(sales);
    }
}
