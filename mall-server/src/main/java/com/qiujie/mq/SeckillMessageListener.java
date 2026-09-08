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
 * <p>
 * 整体流程：
 * <ol>
 *   <li>从 {@code seckill.order.queue} 消费秒杀消息</li>
 *   <li>事务内：幂等防重 → 扣 MySQL 库存 → 创建订单 → 创建订单项</li>
 *   <li>写秒杀成功结果到 Redis（前端轮询感知）</li>
 *   <li>失败时：3 次以内重试，3 次后回滚 Redis 库存并写失败结果</li>
 * </ol>
 * </p>
 *
 * @author qiujie
 */
@Component
@Profile("!test") // 测试环境不注册该 Bean，避免 RabbitMQ 依赖导致测试失败
public class SeckillMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SeckillMessageListener.class);

    // ========== Redis Key 前缀 ==========

    /** 秒杀结果 Key 前缀，完整 Key 为 seckill:result:{sessionId}:{userId}，存 {"status":1/-1} */
    private static final String SECKILL_RESULT_KEY = "seckill:result:";
    /** 用户秒杀防重 Key 前缀，完整 Key 为 seckill:order:{sessionId}:{userId} */
    private static final String SECKILL_ORDER_KEY = "seckill:order:";
    /** 秒杀库存 Key 前缀，完整 Key 为 seckill:stock:{sessionId}，存数字库存值 */
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    // ========== 依赖注入 ==========

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final RedisUtil redisUtil;
    private final RabbitTemplate rabbitTemplate;
    private final TransactionTemplate transactionTemplate; // 编程式事务，精确控制事务边界

    public SeckillMessageListener(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                   ProductMapper productMapper, ProductImgMapper productImgMapper,
                                   RedisUtil redisUtil, RabbitTemplate rabbitTemplate,
                                   TransactionTemplate transactionTemplate) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
        this.rabbitTemplate = rabbitTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    // ========== 重试配置 ==========

    /** 最大重试次数（首次 + 3 次重试 = 总共 4 次尝试） */
    private static final int MAX_RETRIES = 3;
    /** RabbitMQ 消息头，记录当前重试次数（从 0 开始） */
    private static final String RETRY_HEADER = "x-retry-count";

    // ========== 消息入口 ==========

    /**
     * 消费秒杀订单消息。
     * <p>
     * 只有 {@link #createSeckillOrder} 在事务内，ack / 重试 / 回滚都在事务外，
     * 避免「ack 了但事务回滚」或「重试消息发出去又被事务回滚」。
     *
     * @param message    秒杀消息体（sessionId、userId、productId、seckillPrice）
     * @param channel    RabbitMQ Channel，用于手动 ACK
     * @param deliveryTag 投递标签，ACK 时标识具体消息
     * @param retryCount  重试次数 header（null=首次消费，0/1/2=第 1/2/3 次重试）
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE)
    public void handleSeckillOrder(SeckillMessage message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                    @Header(value = RETRY_HEADER, required = false) Integer retryCount) {
        try {
            // ① 事务内创建订单（扣库存 + 插订单 + 插订单项）
            transactionTemplate.executeWithoutResult(status -> createSeckillOrder(message));
            // ② 写秒杀成功结果到 Redis，TTL=120s，供前端轮询读取
            writeSuccessResult(message);
            // ③ 手动 ACK，告诉 RabbitMQ 消息已处理完毕
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // ========== 异常处理：重试 or 回滚 ==========
            // retryCount 为 null 表示首次消费失败，计为第 0 次重试
            int currentRetry = retryCount != null ? retryCount : 0;
            if (currentRetry < MAX_RETRIES) {
                // ---- 未达重试上限：发到重试队列 ----
                // 清除幂等标记，否则重试时 SETNX 返回 false 会跳过订单创建
                String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
                redisUtil.del(processedKey);
                // 发到 seckill.retry.delay.queue（TTL=5s），过期后自动路由回 seckill.order.queue
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.SECKILL_RETRY_QUEUE,
                        message,
                        msg -> {
                            // 重试次数 +1，写入消息头供下次消费时读取
                            msg.getMessageProperties().setHeader(RETRY_HEADER, currentRetry + 1);
                            return msg;
                        });
                // ACK 原消息（已转交到重试队列，原消息不需要留在队列中）
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (IOException ignored) {
                }
            } else {
                // ---- 已达重试上限：回滚库存 + 写失败结果 ----
                log.error("Seckill order failed after {} retries: sessionId={} userId={}",
                        MAX_RETRIES, message.getSessionId(), message.getUserId(), e);
                // 清除幂等标记
                String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
                redisUtil.del(processedKey);
                // Lua 回滚 Redis 库存（+1）
                rollbackStock(message);
                // 写失败结果到 Redis，前端轮询感知到 status=-1
                writeFailureResult(message);
                // ACK 原消息（已做最终处理，不再重试）
                try {
                    channel.basicAck(deliveryTag, false);
                } catch (IOException ignored) {
                }
            }
        }
    }

    // ========== 库存回滚 ==========

    /**
     * 回滚 Redis 秒杀库存。
     * <p>
     * 通过 Lua 脚本原子加 1，比 SET/GET 组合更安全：并发场景下不会出现覆盖丢失。
     * 同时删除用户秒杀防重标记，使该用户可重新参与同场次秒杀。
     */
    private void rollbackStock(SeckillMessage message) {
        // 拼装 Redis Key
        String stockKey = SECKILL_STOCK_KEY + message.getSessionId();       // seckill:stock:3
        String orderKey = SECKILL_ORDER_KEY + message.getSessionId() + ":" + message.getUserId(); // seckill:order:3:5

        String lua = """
            local raw = redis.call('GET', KEYS[1])   -- 读取当前库存
            if not raw then return {-1, 0, 0} end     -- Key 不存在，返回失败标记
            raw = raw:gsub('"', '')                   -- 去掉 RedisTemplate 序列化加的 JSON 引号
            local before = tonumber(raw) or 0          -- 转为数字
            local after = before + 1                   -- 库存 +1
            redis.call('SET', KEYS[1], after)          -- 写回 Redis
            return {1, before, after}                  -- 返回 {成功标记, 回滚前, 回滚后}
            """;
        @SuppressWarnings("unchecked")
        List<Long> result = redisUtil.executeLua(lua, List.class, List.of(stockKey));
        // 解析 Lua 返回值（仅日志用途，回滚操作本身已完成）
        int before = 0, after = 0;
        if (result != null && result.size() >= 3 && result.get(0) == 1L) {
            before = result.get(1).intValue();  // 回滚前库存
            after = result.get(2).intValue();   // 回滚后库存
        }
        redisUtil.del(orderKey); // 删除防重标记，用户可重新秒杀同场次
    }

    // ========== 结果写入 ==========

    /**
     * 写失败结果到 Redis。
     * <p>
     * Key 格式为 seckill:result:{sessionId}:{userId}，TTL=120s。
     * 前端轮询读到 status=-1 后弹出失败提示。
     */
    private void writeFailureResult(SeckillMessage message) {
        String resultKey = SECKILL_RESULT_KEY + message.getSessionId() + ":" + message.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("status", -1);   // -1 = 秒杀失败
        result.put("msg", "秒杀失败");
        redisUtil.set(resultKey, JSONUtil.toJsonStr(result), 120); // TTL=120s
    }

    /**
     * 写成功结果到 Redis。
     * <p>
     * 前端轮询读到 status=1 后弹出成功提示，引导用户去支付。
     */
    private void writeSuccessResult(SeckillMessage message) {
        String resultKey = SECKILL_RESULT_KEY + message.getSessionId() + ":" + message.getUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("status", 1);    // 1 = 秒杀成功
        result.put("msg", "秒杀成功");
        redisUtil.set(resultKey, JSONUtil.toJsonStr(result), 120); // TTL=120s
    }

    // ========== 核心订单创建 ==========

    /**
     * 在事务内创建秒杀订单。
     * <p>
     * 包含以下步骤，全部在同一事务内，任一步骤失败则全部回滚：
     * <ol>
     *   <li>幂等检查：Redis SETNX 防止重复消费</li>
     *   <li>扣 MySQL 商品库存：UPDATE ... WHERE stock >= 1</li>
     *   <li>创建订单：INSERT INTO oms_order</li>
     *   <li>发送超时取消消息：发到 order.delay.queue（TTL=10min）</li>
     *   <li>创建订单项：INSERT INTO oms_order_item</li>
     * </ol>
     *
     * @param message 秒杀消息体
     * @throws ServiceException 库存不足时抛出
     */
    private void createSeckillOrder(SeckillMessage message) {
        // — 1. 幂等检查 —
        // 用 Redis SETNX 标记该消息已处理，120s 后自动过期（覆盖极端重复场景）
        String processedKey = "seckill:processed:" + message.getSessionId() + ":" + message.getUserId();
        if (Boolean.FALSE.equals(redisUtil.setIfAbsent(processedKey, "1", 120))) {
            return; // 已处理过，跳过（幂等）
        }

        // — 2. 原子扣 MongoDB 商品库存 —
        // SQL: UPDATE pms_product SET stock = stock - 1 WHERE id = ? AND stock >= 1
        int affected = productMapper.decrementStock(message.getProductId(), 1);
        if (affected == 0) {
            // 受影响行数为 0 → stock < 1 → 库存不足
            throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT);
        }

        // — 3. 查询商品信息（需要商品名写入订单项） —
        Product product = productMapper.selectById(message.getProductId());

        // — 4. 构建订单实体 —
        Order order = new Order();
        order.setUserId(message.getUserId());              // 购买用户
        order.setSeckillSessionId(message.getSessionId()); // 关联秒杀场次，用于库存恢复
        order.setOrderSn(generateOrderSn());               // 雪花算法生成唯一订单号
        order.setTotalAmount(message.getSeckillPrice());   // 秒杀价格
        order.setPayMethod(PayMethodEnum.UNKNOWN);         // 支付方式待定
        order.setStatus(OrderStatusEnum.PENDING_PAY);      // 状态：待支付（0）

        // — 5. 插入订单（唯一索引兜底防重） —
        try {
            orderMapper.insert(order);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // uk_user_seckill(user_id, seckill_session_id) 唯一索引冲突
            // 说明 Redis 防重标记已过期或缺失，但 DB 层发现该用户已有同场次订单
            // 恢复已扣的商品库存后返回
            productMapper.incrementStock(message.getProductId(), 1);
            return;
        }

        // — 6. 发送超时取消消息 —
        // 发到 order.delay.queue（TTL=10min），10 分钟后路由到 order.timeout.queue
        // OrderTimeoutListener 消费后自动取消未支付订单
        sendTimeoutMessage(order.getId());

        // — 7. 构建订单项 —
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());              // 关联订单
        item.setProductId(product.getId());           // 商品 ID
        item.setProductName(product.getName());       // 商品名称（快照）
        item.setProductPrice(message.getSeckillPrice()); // 成交价（快照）
        item.setAmount(1);                            // 秒杀固定 1 件

        // — 8. 查询商品封面图写入订单项 —
        ProductImg firstImg = productImgMapper.selectFirstByProductId(product.getId());
        if (firstImg != null) {
            item.setProductImg(firstImg.getUrl());
        }
        orderItemMapper.insert(item);

        // — 9. 缓存订单项快照到 Redis —
        // 支付页需要展示订单项信息，存入 Redis 避免查 DB（TTL=1h）
        cacheOrderItems(order.getId(), item);
    }

    // ========== 工具方法 ==========

    /** 安全类型转换，避免 Lua 返回值解析时的 ClassCastException */
    private int toInt(Object val) {
        if (val == null) return 0;
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return 0; }
    }

    /**
     * 生成唯一订单号。
     * <p>
     * 格式：年月日时分秒（14 位）+ 雪花算法 ID 后段（8 位） = 22 位，
     * 示例：2026070215300012345678。
     * <p>
     * Hutool Snowflake workerId=1, datacenterId=1，通过静态方法 nextIdStr() 获取。
     */
    private String generateOrderSn() {
        // 时间前缀：yyyyMMddHHmmss
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                // 雪花 ID 后缀：取 19 位雪花 ID 后 8 位，保证分布式唯一
                + IdUtil.getSnowflake(1, 1).nextIdStr().substring(11);
    }

    /**
     * 发送订单超时取消延时消息。
     * <p>
     * 消息直接发送到 order.delay.queue（TTL=10min），
     * 该队列无消费者，消息过期后通过 DLX 路由到 order.timeout.queue。
     */
    private void sendTimeoutMessage(Integer orderId) {
        OrderTimeoutMessage msg = new OrderTimeoutMessage();
        msg.setOrderId(orderId); // 只传订单 ID，消费者根据 ID 查最新状态
        // 使用默认 exchange，routingKey = 队列名，直接路由到 order.delay.queue
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_DELAY_QUEUE, msg);
    }

    /**
     * 缓存订单项快照到 Redis。
     * <p>
     * 支付页面需要展示订单项（商品名、数量、价格），
     * 缓存到 Redis（TTL=1h）避免每次渲染都查 DB。
     */
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
