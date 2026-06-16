package com.qiujie.mq;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.qiujie.config.RabbitMQConfig;
import com.qiujie.constants.RedisConstants;
import com.qiujie.entity.Order;
import com.qiujie.entity.OrderItem;
import com.qiujie.entity.Product;
import com.qiujie.entity.ProductImg;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.enums.PayMethodEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.OrderItemMapper;
import com.qiujie.mapper.OrderMapper;
import com.qiujie.mapper.ProductImgMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.service.ReconcileLogService;
import com.qiujie.util.RedisUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.transaction.support.TransactionTemplate;

import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单消息消费者
 * <p>
 * 事务边界：仅 {@link #createSeckillOrder} 在事务内执行，ack / 重试 / 回滚在事务外，
 * 避免 ack 被事务回滚影响、也避免重试消息发送被事务拦截。
 * </p>
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class SeckillMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SeckillMessageListener.class);
    private static final String SECKILL_RESULT_KEY = "seckill:result:";
    private static final String SECKILL_ORDER_KEY = "seckill:order:";
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final RedisUtil redisUtil;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate;
    private final ReconcileLogService reconcileLogService;

    public SeckillMessageListener(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                   ProductMapper productMapper, ProductImgMapper productImgMapper,
                                   RedisUtil redisUtil, RabbitTemplate rabbitTemplate,
                                   TransactionTemplate transactionTemplate,
                                   ReconcileLogService reconcileLogService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate = transactionTemplate;
        this.reconcileLogService = reconcileLogService;
    }

    private static final int MAX_RETRIES = 3;
    private static final String RETRY_HEADER = "x-retry-count";

    /**
     * 消费秒杀订单消息，创建订单并扣减库存。
     * 事务仅包裹 {@link #createSeckillOrder}，ack / 重试 / 回滚在事务外处理。
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillOrder(SeckillMessage message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                    @Header(value = RETRY_HEADER, required = false) Integer retryCount) {
        try {
            transactionTemplate.executeWithoutResult(status -> createSeckillOrder(message));
            writeSuccessResult(message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            int currentRetry = retryCount != null ? retryCount : 0;
            if (currentRetry < MAX_RETRIES) {
                // 清除幂等标记，使重试消息可以重新创建订单（原事务已回滚）
                String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
                redisUtil.del(processedKey);
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SECKILL_RETRY_QUEUE,
                        message,
                        msg -> {
                            msg.getMessageProperties().setHeader(RETRY_HEADER, currentRetry + 1);
                            return msg;
                        });
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (IOException ignored) {
                }
            } else {
                log.error("Seckill order failed after {} retries: sessionId={} userId={}",
                        MAX_RETRIES, message.getSessionId(), message.getUserId(), e);
                String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
                redisUtil.del(processedKey);
                rollbackStock(message);
                writeFailureResult(message);
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void rollbackStock(SeckillMessage message) {
        String stockKey = SECKILL_STOCK_KEY + message.getSessionId();
        String orderKey = SECKILL_ORDER_KEY + message.getSessionId() + ":" + message.getUserId();
        // Lua 原子递增库存，避免删 key 导致窗口期数值不准
        String lua = """
            local raw = redis.call('GET', KEYS[1])
            if not raw then return {-1, 0, 0} end
            raw = raw:gsub('"', '')
            local before = tonumber(raw) or 0
            local after = before + 1
            redis.call('SET', KEYS[1], after)
            return {1, before, after}
            """;
        @SuppressWarnings("unchecked")
        List<Long> result = redisUtil.executeLua(lua, List.class, List.of(stockKey));
        int before = 0, after = 0;
        if (result != null && result.size() >= 3 && result.get(0) == 1L) {
            before = result.get(1).intValue();
            after = result.get(2).intValue();
        }
        redisUtil.del(orderKey);
        reconcileLogService.log(message.getSessionId(), message.getUserId(),
                "ROLLBACK", before, after);
    }

    private void writeFailureResult(SeckillMessage message) {
        String resultKey = SECKILL_RESULT_KEY + message.getSessionId() + ":" + message.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("status", -1);
        result.put("msg", "秒杀失败");
        redisUtil.set(resultKey, JSONUtil.toJsonStr(result), 120);
    }

    private void createSeckillOrder(SeckillMessage message) {
        // 幂等检查：用 Redis SETNX 防止重复消费
        String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
        if (Boolean.FALSE.equals(redisUtil.setIfAbsent(processedKey, "1", 120))) {
            return; // 已处理过，跳过
        }

        // 原子扣减 MySQL 库存（UPDATE ... WHERE stock >= delta）
        int affected = productMapper.decrementStock(message.getProductId(), 1);
        if (affected == 0) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT);
        }

        Product product = productMapper.selectById(message.getProductId());

        Order order = new Order();
        order.setUserId(message.getUserId());
        order.setSeckillSessionId(message.getSessionId());
        order.setOrderSn(generateOrderSn());
        order.setTotalAmount(message.getSeckillPrice());
        order.setPayMethod(PayMethodEnum.UNKNOWN);
        order.setStatus(OrderStatusEnum.PENDING_PAY);
        orderMapper.insert(order);
        sendTimeoutMessage(order.getId());

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductPrice(message.getSeckillPrice());
        item.setAmount(1);
        ProductImg firstImg = productImgMapper.selectFirstByProductId(product.getId());
        if (firstImg != null) {
            item.setProductImg(firstImg.getUrl());
        }
        orderItemMapper.insert(item);
        cacheOrderItems(order.getId(), item);
        reconcileLogService.log(message.getSessionId(), message.getUserId(),
                "DEDUCT", product.getStock() + 1, product.getStock());
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private void writeSuccessResult(SeckillMessage message) {
        String resultKey = SECKILL_RESULT_KEY + message.getSessionId() + ":" + message.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("status", 1);
        result.put("msg", "秒杀成功");
        redisUtil.set(resultKey, JSONUtil.toJsonStr(result), 120);
    }

    /**
     * 使用 Hutool 雪花算法（Snowflake）保证分布式环境下的唯一性
     */
    private String generateOrderSn() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.getSnowflake(1, 1).nextIdStr().substring(11);
    }

    private void sendTimeoutMessage(Integer orderId) {
        OrderTimeoutMessage msg = new OrderTimeoutMessage();
        msg.setOrderId(orderId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_DELAY_QUEUE, msg);
    }

    private void cacheOrderItems(Integer orderId, OrderItem... items) {
        java.util.List<Map<String, Object>> snapshot = new java.util.ArrayList<>();
        for (OrderItem item : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", item.getProductId());
            m.put("amount", item.getAmount());
            snapshot.add(m);
        }
        redisUtil.set(RedisConstants.ORDER_ITEMS_SNAPSHOT_KEY + orderId, JSONUtil.toJsonStr(snapshot), 3600);
    }
}
