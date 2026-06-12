package com.qiujie.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiujie.config.RabbitMQConfig;
import com.qiujie.entity.Order;
import com.qiujie.entity.OrderItem;
import com.qiujie.entity.SeckillSession;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.mapper.OrderItemMapper;
import com.qiujie.mapper.OrderMapper;
import com.qiujie.mapper.ProductMapper;
import com.qiujie.mapper.SeckillSessionMapper;
import com.qiujie.util.RedisUtil;
import com.qiujie.util.SalesRankService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单超时取消监听器 — 消费 DLX 路由过来的过期消息，并恢复秒杀库存
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class OrderTimeoutListener {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutListener.class);
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SeckillSessionMapper seckillSessionMapper;
    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;
    private final SalesRankService salesRankService;

    public OrderTimeoutListener(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                                 SeckillSessionMapper seckillSessionMapper, ProductMapper productMapper,
                                 RedisUtil redisUtil, SalesRankService salesRankService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.seckillSessionMapper = seckillSessionMapper;
        this.productMapper = productMapper;
        this.redisUtil = redisUtil;
        this.salesRankService = salesRankService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void handleTimeout(OrderTimeoutMessage message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            Order order = orderMapper.selectById(message.getOrderId());
            if (order != null && order.getStatus() == OrderStatusEnum.PENDING_PAY) {
                order.setStatus(OrderStatusEnum.CANCELLED);
                orderMapper.updateById(order);
                List<OrderItem> items = orderItemMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                                .eq("order_id", order.getId()));
                restoreSeckillStock(order);
                releaseLockStock(items);
                adjustSalesRank(items);
                log.info("Order {} cancelled due to timeout", message.getOrderId());
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process timeout for order {}", message.getOrderId(), e);
            try {
                channel.basicAck(deliveryTag, false);
            } catch (Exception ignored) {
            }
        }
    }

    private void releaseLockStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            productMapper.incrementStock(item.getProductId(), item.getAmount());
        }
    }

    private void adjustSalesRank(List<OrderItem> items) {
        for (OrderItem item : items) {
            salesRankService.recordCancel(item.getProductId(), item.getAmount());
        }
    }

    private void restoreSeckillStock(Order order) {
        try {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            if (items.isEmpty()) return;
            BigDecimal price = order.getTotalAmount();
            Integer productId = items.get(0).getProductId();
            List<SeckillSession> sessions = seckillSessionMapper.selectList(
                    new QueryWrapper<SeckillSession>()
                            .eq("product_id", productId)
                            .eq("seckill_price", price));
            // Lua 原子递增库存，不再删 key
            String lua = """
                local raw = redis.call('GET', KEYS[1])
                if raw then
                    raw = raw:gsub('"', '')
                    redis.call('SET', KEYS[1], tonumber(raw) + 1)
                end
                """;
            for (SeckillSession s : sessions) {
                redisUtil.executeLua(lua, Void.class, List.of(SECKILL_STOCK_KEY + s.getId()));
            }
        } catch (Exception e) {
            log.warn("Failed to restore seckill stock for order {}", order.getId(), e);
        }
    }
}
