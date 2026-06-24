package com.qiujie.benchmark;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiujie.entity.Order;
import com.qiujie.mapper.OrderMapper;
import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 秒杀压测结果验证 — 断言无超卖、无重复订单、库存正确。
 */
@SpringBootTest
@ActiveProfiles("dev")
public class SeckillBenchmarkVerification {

    private static final int SESSION_ID = 99;
    private static final String SECKILL_STOCK_KEY = "seckill:stock:99";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderMapper orderMapper;

    private int getTargetStock() {
        String val = System.getProperty("benchmark.stock", "500");
        return Integer.parseInt(val);
    }

    @Test
    public void verifyAll() {
        int initialStock = getTargetStock();
        int remainingStock = getRemainingStock();
        long orderCount = countSeckillOrders();
        int uniqueUsers = countUniqueUsers();
        int duplicateUsers = countDuplicateUsers();

        System.out.println("=== 秒杀压测验证 ===");
        System.out.println("初始库存: " + initialStock);
        System.out.println("剩余库存: " + remainingStock);
        System.out.println("订单数: " + orderCount);
        System.out.println("去重用户数: " + uniqueUsers);
        System.out.println("重复用户数: " + duplicateUsers);
        System.out.println("预期已售: " + (initialStock - remainingStock));

        // 1. 库存不为负
        assertTrue(remainingStock >= 0,
                "库存不能为负! 当前: " + remainingStock);

        // 2. 订单数 = 初始库存 - 剩余库存
        int expectedSales = initialStock - remainingStock;
        assertEquals(expectedSales, orderCount,
                "订单数(" + orderCount + ") 应等于已售数量(" + expectedSales + ")");

        // 3. 无重复订单
        assertEquals(0, duplicateUsers,
                "存在重复订单! 重复用户数: " + duplicateUsers);

        // 4. 去重用户数 = 订单数
        assertEquals(orderCount, uniqueUsers,
                "去重用户数(" + uniqueUsers + ") 应等于订单数(" + orderCount + ")");

        System.out.println("✅ 验证通过!");
    }

    @Test
    public void printStats() {
        System.out.println("=== 秒杀压测统计 ===");
        System.out.println("剩余库存: " + getRemainingStock());
        System.out.println("订单数: " + countSeckillOrders());
        System.out.println("去重用户数: " + countUniqueUsers());
    }

    private int getRemainingStock() {
        Object val = redisUtil.get(SECKILL_STOCK_KEY);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString().replaceAll("^\"|\"$", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long countSeckillOrders() {
        return orderMapper.selectCount(
                new QueryWrapper<Order>().eq("seckill_session_id", SESSION_ID));
    }

    private int countUniqueUsers() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().eq("seckill_session_id", SESSION_ID));
        return (int) orders.stream().map(Order::getUserId).distinct().count();
    }

    private int countDuplicateUsers() {
        List<Order> orders = orderMapper.selectList(
                new QueryWrapper<Order>().eq("seckill_session_id", SESSION_ID));
        Map<Integer, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(Order::getUserId, Collectors.counting()));
        return (int) counts.values().stream().filter(c -> c > 1).count();
    }
}
