package com.qiujie.service.impl;

import com.qiujie.mapper.*;
import com.qiujie.service.HomeService;
import com.qiujie.service.UserService;
import com.qiujie.util.DatetimeUtil;
import com.qiujie.vo.CategorySalesVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeServiceImpl implements HomeService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final DatetimeUtil datetimeUtil;

    public HomeServiceImpl(UserService userService, UserMapper userMapper,
                           ProductMapper productMapper, OrderMapper orderMapper,
                           DatetimeUtil datetimeUtil) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.datetimeUtil = datetimeUtil;
    }

    public Map<String, Object> getCount() {
        Map<String, Object> map = new HashMap<>();
        map.put("userCount", this.userMapper.selectCount(null));
        map.put("productCount", this.productMapper.selectCount(null));
        map.put("orderCount", this.orderMapper.selectCount(null));

        // 今日/昨日统计用聚合 SQL，避免拉全量订单到内存
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();

        Map<String, Object> todayStats = this.orderMapper.selectDailyStats(todayStart, todayEnd);
        map.put("todayOrderCount", todayStats.get("orderCount"));
        map.put("todaySales", todayStats.get("totalSales"));

        Map<String, Object> yesterdayStats = this.orderMapper.selectDailyStats(yesterdayStart, todayStart);
        map.put("yesterdayOrderCount", yesterdayStats.get("orderCount"));
        map.put("yesterdaySales", yesterdayStats.get("totalSales"));

        return map;
    }

    @Override
    public List<Map<String, Object>> getTrend(Integer days) {
        return this.orderMapper.selectTrendData(days);
    }

    @Override
    public List<CategorySalesVO> getCategorySales() {
        return this.orderMapper.selectCategorySales();
    }
}
