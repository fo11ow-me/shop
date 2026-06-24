package com.qiujie.benchmark;

import cn.dev33.satoken.dao.SaTokenDao;
import com.qiujie.entity.User;
import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.UserStatusEnum;
import com.qiujie.mapper.UserMapper;
import com.qiujie.mapper.SeckillSessionMapper;
import com.qiujie.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 秒杀压测数据准备 — 批量创建测试用户，直接写 Redis 生成 Sa-Token。
 * <p>
 * 运行: mvn test -Dtest=SeckillBenchmarkSetup#generateUsers
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
public class SeckillBenchmarkSetup {

    private static final int USER_COUNT = 1000;
    private static final String TOKEN_CSV = "target/benchmark-tokens.csv";
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SeckillSessionMapper sessionMapper;

    @Autowired
    private SaTokenDao saTokenDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 生成 1000 个测试用户，用 SaTokenDao 直接写 Redis 创建 Token，输出 CSV。
     */
    @Test
    public void generateUsers() throws IOException {
        System.out.println("=== 秒杀压测数据准备 ===");

        // 1. 清理旧测试用户 + 创建新用户
        System.out.println("准备 " + USER_COUNT + " 个测试用户...");
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < USER_COUNT; i++) {
            int uid = 10000 + i;
            // 如果用户已存在则跳过（上次运行残留）
            if (userMapper.selectById(uid) != null) {
                continue;
            }
            User user = new User();
            user.setId(uid);
            user.setCode("bench" + i);
            user.setName("压测用户" + i);
            user.setPassword("123");
            user.setPhone("1380000" + String.format("%04d", i));
            user.setStatus(UserStatusEnum.ENABLED);
            user.setCreateTime(java.sql.Timestamp.valueOf(now));
            userMapper.insert(user);
        }
        System.out.println("用户创建完成");

        // 3. 用 SaTokenDao 原生 API 写 Token（避免 Jackson 序列化加引号问题）
        System.out.println("生成 token...");
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_CSV))) {
            pw.println("token,userId");
            for (int i = 0; i < USER_COUNT; i++) {
                String token = UUID.randomUUID().toString().replace("-", "");
                saTokenDao.set("mall-token:login:token:" + token,
                        String.valueOf(10000 + i), 7200);
                pw.println(token + "," + (10000 + i));
                if ((i + 1) % 200 == 0) {
                    System.out.println("  已生成 " + (i + 1) + " / " + USER_COUNT + " 个 token");
                }
            }
        }
        System.out.println("Token 生成完成: " + TOKEN_CSV);

        // 4. 准备秒杀场次 sessionId=99
        jdbcTemplate.update("DELETE FROM oms_order_item WHERE order_id IN (SELECT id FROM oms_order WHERE seckill_session_id = 99)");
        jdbcTemplate.update("DELETE FROM oms_order WHERE seckill_session_id = 99");
        jdbcTemplate.update("DELETE FROM sms_seckill_session WHERE id = 99");
        SeckillSession session = new SeckillSession();
        session.setId(99);
        session.setProductId(19);
        session.setSeckillPrice(new BigDecimal("50.00"));
        session.setSeckillStock(500);
        session.setStartTime(LocalDateTime.now().minusDays(1));
        session.setEndTime(LocalDateTime.now().plusDays(30));
        sessionMapper.insert(session);
        System.out.println("场次创建: sessionId=99, productId=19");

        // 5. 初始化 Redis 库存
        redisUtil.set(SECKILL_STOCK_KEY + 99, "500");
        System.out.println("Redis 库存初始化: seckill:stock:99 = 500");
        System.out.println("=== 数据准备完成 ===");
    }
}
