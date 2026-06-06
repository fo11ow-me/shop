package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiujie.entity.Order;
import com.qiujie.mapper.*;
import com.qiujie.service.HomeService;
import com.qiujie.service.UserService;
import com.qiujie.util.DatetimeUtil;
import com.qiujie.vo.CategorySalesVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

        // 今日订单统计
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        QueryWrapper<Order> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("create_time", todayStart).lt("create_time", todayEnd);
        List<Order> todayOrders = this.orderMapper.selectList(todayWrapper);
        map.put("todayOrderCount", (long) todayOrders.size());
        map.put("todaySales", todayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 昨日订单统计
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = todayStart;
        QueryWrapper<Order> yesterdayWrapper = new QueryWrapper<>();
        yesterdayWrapper.ge("create_time", yesterdayStart).lt("create_time", yesterdayEnd);
        List<Order> yesterdayOrders = this.orderMapper.selectList(yesterdayWrapper);
        map.put("yesterdayOrderCount", (long) yesterdayOrders.size());
        map.put("yesterdaySales", yesterdayOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

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
