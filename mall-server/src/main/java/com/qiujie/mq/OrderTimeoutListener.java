package com.qiujie.mq;

import com.qiujie.config.RabbitMQConfig;
import com.qiujie.entity.Order;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 订单超时取消监听器 — 消费 DLX 路由过来的过期消息
 *
 * @author qiujie
 */
@Component
@Profile("!test")
public class OrderTimeoutListener {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutListener.class);

    private final OrderMapper orderMapper;

    public OrderTimeoutListener(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_TIMEOUT_QUEUE)
    public void handleTimeout(OrderTimeoutMessage message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            Order order = orderMapper.selectById(message.getOrderId());
            if (order != null && order.getStatus() == OrderStatusEnum.PENDING_PAY) {
                order.setStatus(OrderStatusEnum.CANCELLED);
                orderMapper.updateById(order);
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
}
