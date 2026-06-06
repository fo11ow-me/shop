package com.qiujie.service.impl;

import static com.qiujie.constants.RedisConstants.SECKILL_STOCK_KEY;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.config.RabbitMQConfig;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.mapper.SeckillSessionMapper;
import com.qiujie.mq.SeckillMessage;
import com.qiujie.service.SeckillService;
import com.qiujie.util.RedisUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动业务实现
 *
 * @author qiujie
 */
@Service
@Profile("!test")
public class SeckillServiceImpl extends ServiceImpl<SeckillSessionMapper, SeckillSession> implements SeckillService {

    private static final String SECKILL_ORDER_KEY = "seckill:order:";
    private static final String SECKILL_RESULT_KEY = "seckill:result:";

    private final SeckillSessionMapper seckillSessionMapper;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final RedisUtil redisUtil;
    private final RabbitTemplate rabbitTemplate;

    public SeckillServiceImpl(SeckillSessionMapper seckillSessionMapper,
                              ProductMapper productMapper,
                              ProductImgMapper productImgMapper,
                              RedisUtil redisUtil,
                              RabbitTemplate rabbitTemplate) {
        this.seckillSessionMapper = seckillSessionMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public List<Map<String, Object>> getActiveSessions() {
        List<SeckillSession> sessions = seckillSessionMapper.selectActiveSessions(LocalDateTime.now());
        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            String stockKey = SECKILL_STOCK_KEY + session.getId();
            if (redisUtil.get(stockKey) == null) {
                redisUtil.set(stockKey, String.valueOf(session.getSeckillStock()));
            }
            result.add(sessionToMap(session));
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getUpcomingSessions() {
        List<SeckillSession> sessions = seckillSessionMapper.selectUpcomingSessions(LocalDateTime.now());
        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            result.add(sessionToMap(session));
        }
        return result;
    }

    @Override
    public void execute(Integer sessionId, Integer userId) {
        // 1. 校验秒杀场次存在且进行中
        SeckillSession session = seckillSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }

        // 2. Lua 脚本：原子执行「库存初始化 + 防重检查 + 扣库存 + 标记用户」
        String stockKey = SECKILL_STOCK_KEY + sessionId;
        String orderKey = SECKILL_ORDER_KEY + sessionId + ":" + userId;
        String lua = """
            local stockKey = KEYS[1]
            local orderKey = KEYS[2]
            local initStock = tonumber(ARGV[1])
            local orderTtl = tonumber(ARGV[2])

            -- 库存不存在时初始化
            if redis.call('EXISTS', stockKey) == 0 then
                redis.call('SET', stockKey, initStock)
            end

            -- 防重：同一用户同一场次只能秒杀一次
            if redis.call('EXISTS', orderKey) == 1 then
                return -1  -- 重复秒杀
            end

            -- 检查库存
            local stock = tonumber(redis.call('GET', stockKey) or '0')
            if stock <= 0 then
                return -2  -- 库存不足
            end

            -- 扣减库存 + 标记用户
            redis.call('DECR', stockKey)
            redis.call('SET', orderKey, '1', 'EX', orderTtl)
            return 1  -- 秒杀成功
            """;

        Long result = redisUtil.executeLua(lua, Long.class,
                List.of(stockKey, orderKey),
                session.getSeckillStock(), 86400);
        if (result == null) {
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
        if (result == -1) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_DUPLICATE);
        }
        if (result == -2) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_STOCK_EMPTY);
        }

        // 3. 发送消息到 RabbitMQ，异步创建订单
        SeckillMessage message = new SeckillMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setProductId(session.getProductId());
        message.setSeckillPrice(session.getSeckillPrice());
        rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY, message);
    }

    @Override
    public Map<String, Object> getResult(Integer sessionId, Integer userId) {
        String resultKey = SECKILL_RESULT_KEY + sessionId + ":" + userId;
        Object cached = redisUtil.get(resultKey);
        if (cached != null) {
            return JSONUtil.parseObj(cached.toString());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 0);
        result.put("msg", "排队中");
        return result;
    }

    private Map<String, Object> sessionToMap(SeckillSession session) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sessionId", session.getId());
        map.put("productId", session.getProductId());

        Product product = productMapper.selectById(session.getProductId());
        if (product != null) {
            map.put("productName", product.getName());
            map.put("productPrice", product.getPrice());
            ProductImg img = productImgMapper.selectFirstByProductId(product.getId());
            if (img != null) {
                map.put("productImg", img.getUrl());
            }
        }

        map.put("seckillPrice", session.getSeckillPrice());
        map.put("remainingStock", getRemainingStock(session.getId(), session.getSeckillStock()));
        map.put("startTime", session.getStartTime());
        map.put("endTime", session.getEndTime());
        return map;
    }

    private Integer getRemainingStock(Integer sessionId, Integer dbStock) {
        Object cached = redisUtil.get(SECKILL_STOCK_KEY + sessionId);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }
        return dbStock;
    }
}
