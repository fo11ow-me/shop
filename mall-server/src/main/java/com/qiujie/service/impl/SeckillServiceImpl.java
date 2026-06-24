package com.qiujie.service.impl;

import static com.qiujie.constants.RedisConstants.*;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀活动业务实现
 *
 * @author qiujie
 */
@Service
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
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getActiveSessions() {
        String cached = (String) redisUtil.get(CACHE_SECKILL_ACTIVE_KEY);
        if (cached != null) {
            return (List<Map<String, Object>>) (List<?>) JSONUtil.toList(cached, Map.class);
        }

        List<SeckillSession> sessions = seckillSessionMapper.selectActiveSessions(LocalDateTime.now());
        Map<Integer, Product> productMap = batchProducts(sessions);
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            result.add(sessionToMap(session, productMap.get(session.getProductId()),
                    imgMap.get(session.getProductId())));
        }
        redisUtil.set(CACHE_SECKILL_ACTIVE_KEY, JSONUtil.toJsonStr(result), CACHE_SECKILL_SESSION_TTL);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getUpcomingSessions() {
        String cached = (String) redisUtil.get(CACHE_SECKILL_UPCOMING_KEY);
        if (cached != null) {
            return (List<Map<String, Object>>) (List<?>) JSONUtil.toList(cached, Map.class);
        }

        List<SeckillSession> sessions = seckillSessionMapper.selectUpcomingSessions(LocalDateTime.now());
        Map<Integer, Product> productMap = batchProducts(sessions);
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SeckillSession session : sessions) {
            result.add(sessionToMap(session, productMap.get(session.getProductId()),
                    imgMap.get(session.getProductId())));
        }
        redisUtil.set(CACHE_SECKILL_UPCOMING_KEY, JSONUtil.toJsonStr(result), CACHE_SECKILL_SESSION_TTL);
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

            if redis.call('EXISTS', stockKey) == 0 then
                redis.call('SET', stockKey, initStock)
            end

            if redis.call('EXISTS', orderKey) == 1 then
                return -1
            end

            local raw = redis.call('GET', stockKey) or '0'
            raw = raw:gsub('"', '')
            local stock = tonumber(raw)
            if stock <= 0 then
                return -2
            end

            redis.call('SET', stockKey, stock - 1)
            redis.call('SET', orderKey, '1', 'EX', orderTtl)
            return 1
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

        // 3. 失效秒杀缓存，确保下次查询获取最新库存
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);

        // 4. 发送消息到 RabbitMQ，异步创建订单
        SeckillMessage message = new SeckillMessage();
        message.setSessionId(sessionId);
        message.setUserId(userId);
        message.setProductId(session.getProductId());
        message.setSeckillPrice(session.getSeckillPrice());
        if (rabbitTemplate == null) {
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.SECKILL_EXCHANGE,
                    RabbitMQConfig.SECKILL_ROUTING_KEY, message);
        } catch (Exception e) {
            // RabbitMQ 不可用时原子回滚库存，避免删 key 丢失已扣减量
            String rollbackLua = """
                local raw = redis.call('GET', KEYS[1])
                if raw then
                    raw = raw:gsub('"', '')
                    redis.call('SET', KEYS[1], tonumber(raw) + 1)
                end
                redis.call('DEL', KEYS[2])
                """;
            redisUtil.executeLua(rollbackLua, Void.class, List.of(stockKey, orderKey));
            throw new ServiceException(BusinessStatusEnum.ERROR);
        }
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

    @Override
    public Map<String, Object> listPage(Integer current, Integer size, Integer status) {
        Page<SeckillSession> page = new Page<>(current, size);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SeckillSession> wrapper = null;
        if (status != null) {
            wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            LocalDateTime now = LocalDateTime.now();
            if (status == 0) {
                wrapper.le("start_time", now).ge("end_time", now);
            } else if (status == 1) {
                wrapper.gt("start_time", now);
            } else if (status == 2) {
                wrapper.lt("end_time", now);
            }
        }
        if (wrapper == null) {
            wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.orderByDesc("create_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        IPage<SeckillSession> result = seckillSessionMapper.selectPage(page, wrapper);

        List<SeckillSession> sessions = result.getRecords();
        Map<Integer, Product> productMap = batchProducts(sessions);
        Map<Integer, ProductImg> imgMap = batchProductImages(sessions);

        for (SeckillSession session : sessions) {
            Product product = productMap.get(session.getProductId());
            if (product != null) {
                session.setProductName(product.getName());
                session.setProductPrice(product.getPrice());
            }
            ProductImg img = imgMap.get(session.getProductId());
            if (img != null) {
                session.setProductImg(img.getUrl());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", result.getRecords());
        data.put("total", result.getTotal());
        data.put("current", result.getCurrent());
        data.put("size", result.getSize());
        return data;
    }

    @Override
    public void create(SeckillSession session) {
        if (session.getSeckillPrice() == null || session.getSeckillStock() == null
                || session.getStartTime() == null || session.getEndTime() == null) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "结束时间必须晚于开始时间");
        }
        Product product = productMapper.selectById(session.getProductId());
        if (product == null) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_NOT_EXIST);
        }
        seckillSessionMapper.insert(session);
        // 初始化 Redis 库存
        redisUtil.set(SECKILL_STOCK_KEY + session.getId(), String.valueOf(session.getSeckillStock()));
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    @Override
    public void updateSession(SeckillSession session) {
        SeckillSession existing = seckillSessionMapper.selectById(session.getId());
        if (existing == null) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        if (existing.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }
        if (!session.getEndTime().isAfter(session.getStartTime())) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "结束时间必须晚于开始时间");
        }
        Product product = productMapper.selectById(session.getProductId());
        if (product != null && session.getSeckillPrice() != null
                && session.getSeckillPrice().compareTo(product.getPrice()) >= 0) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR.getCode(), "秒杀价必须低于商品原价");
        }
        seckillSessionMapper.updateById(session);
        // 同步缓存
        redisUtil.set(SECKILL_STOCK_KEY + session.getId(), String.valueOf(session.getSeckillStock()));
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    @Override
    public void deleteSession(Integer id) {
        SeckillSession session = seckillSessionMapper.selectById(id);
        if (session == null) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_NOT_EXIST);
        }
        if (session.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(BusinessStatusEnum.SECKILL_SESSION_EXPIRED);
        }
        seckillSessionMapper.deleteById(id);
        redisUtil.del(SECKILL_STOCK_KEY + id);
        redisUtil.del(CACHE_SECKILL_ACTIVE_KEY);
        redisUtil.del(CACHE_SECKILL_UPCOMING_KEY);
    }

    private Map<String, Object> sessionToMap(SeckillSession session, Product product, ProductImg img) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sessionId", session.getId());
        map.put("productId", session.getProductId());

        if (product != null) {
            map.put("productName", product.getName());
            map.put("productPrice", product.getPrice());
            if (img != null) {
                map.put("productImg", img.getUrl());
            }
        }

        map.put("seckillPrice", session.getSeckillPrice());
        map.put("totalStock", session.getSeckillStock());
        map.put("remainingStock", getRemainingStock(session.getId(), session.getSeckillStock()));
        map.put("startTime", session.getStartTime());
        map.put("endTime", session.getEndTime());
        return map;
    }

    /**
     * 批量查询秒杀场次关联的商品
     */
    private Map<Integer, Product> batchProducts(List<SeckillSession> sessions) {
        if (sessions.isEmpty()) return Map.of();
        List<Integer> productIds = sessions.stream()
                .map(SeckillSession::getProductId).distinct().toList();
        return productMapper.selectBatchIds(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        Product::getId, p -> p, (a, b) -> a));
    }

    /**
     * 批量查询秒杀场次关联的商品首图
     */
    private Map<Integer, ProductImg> batchProductImages(List<SeckillSession> sessions) {
        if (sessions.isEmpty()) return Map.of();
        List<Integer> productIds = sessions.stream()
                .map(SeckillSession::getProductId).distinct().toList();
        List<ProductImg> imgs = productImgMapper.selectByProductIds(productIds);
        return imgs.stream().collect(java.util.stream.Collectors.toMap(
                ProductImg::getProductId, img -> img, (a, b) -> a));
    }

    private Integer getRemainingStock(Integer sessionId, Integer dbStock) {
        Object cached = redisUtil.get(SECKILL_STOCK_KEY + sessionId);
        if (cached != null) {
            return Integer.parseInt(cached.toString());
        }
        return dbStock;
    }
}
