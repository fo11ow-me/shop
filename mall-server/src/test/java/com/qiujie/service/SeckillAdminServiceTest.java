package com.qiujie.service;

import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static com.qiujie.constants.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Seckill admin service tests")
class SeckillAdminServiceTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void clearCache() {
        Set<String> keys = stringRedisTemplate.keys("cache:seckill:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("create — 创建秒杀场次成功")
    void shouldCreateSeckillSession() {
        SeckillSession session = new SeckillSession();
        session.setProductId(1);
        session.setSeckillPrice(new BigDecimal("99.00"));
        session.setSeckillStock(50);
        session.setStartTime(LocalDateTime.now().plusDays(1));
        session.setEndTime(LocalDateTime.now().plusDays(2));

        assertDoesNotThrow(() -> seckillService.create(session));
    }

    @Test
    @DisplayName("listPage — 分页查询秒杀场次")
    void shouldListSeckillSessions() {
        Map<String, Object> result = seckillService.listPage(1, 10, null);

        assertNotNull(result);
        assertNotNull(result.get("records"));
        assertNotNull(result.get("total"));
    }

    @Test
    @DisplayName("listPage — 按状态筛选")
    void shouldFilterByStatus() {
        Map<String, Object> result = seckillService.listPage(1, 10, 1);

        assertNotNull(result);
        assertNotNull(result.get("records"));
    }

    @Test
    @DisplayName("updateSession — 更新秒杀场次")
    void shouldUpdateSeckillSession() {
        SeckillSession session = new SeckillSession();
        session.setId(1);
        session.setProductId(1);
        session.setSeckillPrice(new BigDecimal("79.00"));
        session.setSeckillStock(30);
        session.setStartTime(LocalDateTime.now().plusDays(1));
        session.setEndTime(LocalDateTime.now().plusDays(2));

        assertDoesNotThrow(() -> seckillService.updateSession(session));
    }

    @Test
    @DisplayName("updateSession — 更新不存在的场次抛出异常")
    void shouldThrowOnUpdateMissingSession() {
        SeckillSession session = new SeckillSession();
        session.setId(99999);
        session.setProductId(1);
        session.setSeckillPrice(new BigDecimal("79.00"));
        session.setSeckillStock(30);
        session.setStartTime(LocalDateTime.now().plusDays(1));
        session.setEndTime(LocalDateTime.now().plusDays(2));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> seckillService.updateSession(session));
        assertEquals(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("deleteSession — 删除秒杀场次")
    void shouldDeleteSeckillSession() {
        assertDoesNotThrow(() -> seckillService.deleteSession(2));
    }

    @Test
    @DisplayName("deleteSession — 删除不存在的场次抛出异常")
    void shouldThrowOnDeleteMissingSession() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> seckillService.deleteSession(99999));
        assertEquals(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST.getCode(), ex.getCode());
    }
}
