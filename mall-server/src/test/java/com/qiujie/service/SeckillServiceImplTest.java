package com.qiujie.service;

import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.qiujie.constants.RedisConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SeckillServiceImpl tests")
class SeckillServiceImplTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void clearCache() {
        Set<String> keys = stringRedisTemplate.keys("cache:seckill:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        keys = stringRedisTemplate.keys("seckill:stock:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("getActiveSessions — returns sessions with product info")
    void shouldReturnActiveSessions() {
        List<Map<String, Object>> sessions = seckillService.getActiveSessions();

        assertNotNull(sessions);
        assertFalse(sessions.isEmpty());
        Map<String, Object> session = sessions.get(0);
        assertNotNull(session.get("productName"));
        assertEquals(5999, ((Number) session.get("seckillPrice")).intValue());
    }

    @Test
    @DisplayName("getActiveSessions — caches result, second call returns cached")
    void shouldCacheActiveSessions() {
        List<Map<String, Object>> first = seckillService.getActiveSessions();
        List<Map<String, Object>> second = seckillService.getActiveSessions();

        assertEquals(first.size(), second.size());
        assertEquals(first.get(0).get("seckillPrice"), second.get(0).get("seckillPrice"));
    }

    @Test
    @DisplayName("getUpcomingSessions — returns future sessions")
    void shouldReturnUpcomingSessions() {
        List<Map<String, Object>> sessions = seckillService.getUpcomingSessions();

        assertNotNull(sessions);
        assertFalse(sessions.isEmpty());
    }

    @Test
    @DisplayName("execute — seckill succeeds with valid session")
    void shouldExecuteSeckillSuccessfully() {
        // Session 1 is active, user 2 exists
        assertDoesNotThrow(() -> seckillService.execute(1, 2));
    }

    @Test
    @DisplayName("execute — throws on duplicate seckill")
    void shouldThrowOnDuplicateSeckill() {
        seckillService.execute(1, 2);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> seckillService.execute(1, 2));
        assertEquals(BusinessStatusEnum.SECKILL_DUPLICATE.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("execute — throws on expired session")
    void shouldThrowOnExpiredSession() {
        // Session 2 starts in 2099, not yet started
        ServiceException ex = assertThrows(ServiceException.class,
                () -> seckillService.execute(2, 2));
        assertEquals(BusinessStatusEnum.SECKILL_SESSION_EXPIRED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("execute — throws on non-existent session")
    void shouldThrowOnMissingSession() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> seckillService.execute(99999, 2));
        assertEquals(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("getResult — returns pending status before order created")
    void shouldReturnPendingResult() {
        Map<String, Object> result = seckillService.getResult(1, 2);

        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }
}
