package com.qiujie.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.*;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.enums.PayMethodEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.*;
import com.qiujie.service.OrderService;
import com.qiujie.config.RabbitMQConfig;
import com.qiujie.mq.OrderTimeoutMessage;
import com.qiujie.constants.RedisConstants;
import com.qiujie.util.RedisUtil;
import com.qiujie.util.SalesRankService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductImgMapper productImgMapper;
    private final RedisUtil redisUtil;
    private final SalesRankService salesRankService;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartMapper cartMapper, ProductMapper productMapper,
                            ProductImgMapper productImgMapper, RedisUtil redisUtil,
                            SalesRankService salesRankService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
        this.salesRankService = salesRankService;
    }

    @Transactional
    public Order createFromCart(Integer userId, Map<String, Object> params) {
        Integer addressId = (Integer) params.get("addressId");
        if (addressId == null || addressId <= 0) {
            throw new ServiceException(BusinessStatusEnum.ADDRESS_NOT_EXIST);
        }
        Integer payMethodCode = (Integer) params.getOrDefault("payMethod", 0);
        PayMethodEnum payMethod = PayMethodEnum.values()[payMethodCode];

        // Redis 分布式锁防止并发重复下单
        String lockKey = "order:lock:" + userId;
        Boolean locked = redisUtil.setIfAbsent(lockKey, "1", 5);
        if (!Boolean.TRUE.equals(locked)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_IN_PROGRESS);
        }
        try {
            return doCreateFromCart(userId, params, addressId, payMethod);
        } finally {
            redisUtil.del(lockKey);
        }
    }

    private Order doCreateFromCart(Integer userId, Map<String, Object> params,
                                    Integer addressId, PayMethodEnum payMethod) {
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<Cart> selectedCarts = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Integer> cartIds = (List<Integer>) params.get("cartIds");
        if (cartIds != null && !cartIds.isEmpty()) {
            for (Cart cart : cartList) {
                if (cartIds.contains(cart.getId())) {
                    selectedCarts.add(cart);
                }
            }
        } else {
            for (Cart cart : cartList) {
                if (cart.getIsSelected() != null && cart.getIsSelected() == 1) {
                    selectedCarts.add(cart);
                }
            }
        }
        if (selectedCarts.isEmpty()) {
            throw new ServiceException(BusinessStatusEnum.ORDER_EMPTY);
        }

        // 批量查询商品，避免 N+1
        List<Integer> productIds = selectedCarts.stream().map(Cart::getProductId).distinct().toList();
        Map<Integer, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Order order = buildOrder(userId, addressId, payMethod, params);
        save(order);
        sendTimeoutMessage(order.getId());
        for (Cart cart : selectedCarts) {
            Product product = productMap.get(cart.getProductId());
            if (product == null) {
                throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT.getCode(), "商品[" + cart.getProductName() + "]不存在");
            }
            if (productMapper.decrementStock(product.getId(), cart.getAmount()) == 0) {
                throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT.getCode(), "商品[" + cart.getProductName() + "]库存不足");
            }
            order.setTotalAmount(order.getTotalAmount().add(product.getPrice().multiply(new BigDecimal(cart.getAmount()))));
            addOrderItem(order, product.getId(), product.getName(), product.getPrice(), cart.getProductImg(), cart.getAmount());
        }
        updateById(order);
        cacheOrderItems(order.getId());

        for (Cart cart : selectedCarts) {
            cartMapper.deleteById(cart.getId());
        }

        return order;
    }

    @Transactional
    public Order buyNow(Integer userId, Map<String, Object> params) {
        Integer productId = (Integer) params.get("productId");
        Integer amount = (Integer) params.getOrDefault("amount", 1);
        if (productId == null || amount == null || amount < 1) {
            throw new ServiceException(BusinessStatusEnum.PARAM_ERROR);
        }

        if (productMapper.decrementStock(productId, amount) == 0) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT);
        }

        Product product = productMapper.selectById(productId);
        Order order = buildOrder(userId, null, PayMethodEnum.UNKNOWN, params);
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(amount)));
        save(order);
        sendTimeoutMessage(order.getId());

        ProductImg firstImg = productImgMapper.selectFirstByProductId(product.getId());
        String imgUrl = firstImg != null ? firstImg.getUrl() : null;
        OrderItem item = addOrderItem(order, product.getId(), product.getName(), product.getPrice(), imgUrl, amount);
        cacheOrderItems(order.getId());

        order.setItems(List.of(item));
        return order;
    }

    public void pay(Integer userId, Integer id, Integer payMethod) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatusEnum.PENDING_PAY) {
            throw new ServiceException(BusinessStatusEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.PAID);
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentSn("PAY" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000)));
        if (payMethod != null && payMethod > 0) {
            order.setPayMethod(PayMethodEnum.values()[payMethod]);
        }
        updateById(order);
        adjustSalesRank(id, 1);
    }

    public List<Order> list(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    public List<Order> listByStatus(Integer userId, Integer status) {
        List<Order> all = orderMapper.selectByUserId(userId);
        return all.stream().filter(o -> o.getStatus() != null && o.getStatus().getCode() == status).toList();
    }

    public Order detail(Integer userId, Integer id) {
        Order order = orderMapper.selectDetailById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        return order;
    }

    public void delete(Integer userId, Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        removeById(id);
    }

    public IPage<Order> list(Integer current, Integer size, String orderSn, String userName,
                                String status, String startTime, String endTime) {
        Page<Order> page = new Page<>(current, size);
        List<Integer> statusList = null;
        if (status != null && !status.isEmpty()) {
            statusList = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return orderMapper.selectPageWithParams(page, orderSn, userName, statusList, startTime, endTime);
    }

    public Order detail(Integer id) {
        return orderMapper.selectDetailById(id);
    }

    public void deliver(Integer id) {
        Order order = getById(id);
        if (order != null && order.getStatus() == OrderStatusEnum.PAID) {
            order.setStatus(OrderStatusEnum.SHIPPED);
            order.setDeliveryTime(LocalDateTime.now());
            updateById(order);
        }
    }

    public void cancel(Integer id) {
        Order order = getById(id);
        if (order == null) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatusEnum.PENDING_PAY) {
            throw new ServiceException(BusinessStatusEnum.ORDER_CANCEL_DENIED);
        }
        order.setStatus(OrderStatusEnum.CANCELLED);
        updateById(order);
        // 释放锁定库存
        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                        .eq("order_id", id));
        for (OrderItem item : items) {
            productMapper.incrementStock(item.getProductId(), item.getAmount());
        }
        adjustSalesRank(id, -1);
    }

    public void receipt(Integer userId, Integer id) {
        Order order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatusEnum.SHIPPED) {
            throw new ServiceException(BusinessStatusEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(OrderStatusEnum.COMPLETED);
        order.setReceiptTime(LocalDateTime.now());
        updateById(order);
    }

    public void updateRecipient(Integer userId, Integer id, String recipientName, String recipientPhone, String recipientAddress, Integer expressDelivery) {
        Order order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new ServiceException(BusinessStatusEnum.ORDER_NOT_EXIST);
        }
        if (order.getStatus() != OrderStatusEnum.PENDING_PAY) {
            throw new ServiceException(BusinessStatusEnum.ORDER_STATUS_ERROR);
        }
        order.setRecipientName(recipientName);
        order.setRecipientPhone(recipientPhone);
        order.setRecipientAddress(recipientAddress);
        if (expressDelivery != null) {
            order.setExpressDelivery(expressDelivery);
        }
        updateById(order);
    }

    public void batchDeliver(List<Integer> ids) {
        List<Order> orders = listByIds(ids);
        List<Order> toUpdate = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatusEnum.PAID) {
                order.setStatus(OrderStatusEnum.SHIPPED);
                order.setDeliveryTime(LocalDateTime.now());
                toUpdate.add(order);
            }
        }
        if (!toUpdate.isEmpty()) {
            updateBatchById(toUpdate);
        }
    }

    public void delete(Integer id) {
        removeById(id);
    }

    public void batchDelete(List<Integer> ids) {
        removeBatchByIds(ids);
    }

    /**
     * 构建订单骨架，填充收件人、配送方式等公共字段
     */
    private Order buildOrder(Integer userId, Integer addressId, PayMethodEnum payMethod, Map<String, Object> params) {
        Order order = new Order();
        order.setUserId(userId);
        if (addressId != null) order.setAddressId(addressId);
        order.setOrderSn(generateOrderSn());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayMethod(payMethod);
        order.setStatus(OrderStatusEnum.PENDING_PAY);
        order.setRecipientName((String) params.getOrDefault("recipientName", ""));
        order.setRecipientPhone((String) params.getOrDefault("recipientPhone", ""));
        order.setRecipientAddress((String) params.getOrDefault("recipientAddress", ""));
        order.setExpressDelivery((Integer) params.getOrDefault("expressDelivery", 0));
        return order;
    }

    /**
     * 创建订单明细项并持久化
     */
    private OrderItem addOrderItem(Order order, Integer productId, String productName,
                                    BigDecimal price, String img, Integer amount) {
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(productId);
        item.setProductName(productName);
        item.setProductPrice(price);
        item.setAmount(amount);
        if (img != null) item.setProductImg(img);
        orderItemMapper.insert(item);
        return item;
    }

    /**
     * 生成全局唯一订单号
     * <p>
     * 使用 Hutool 雪花算法（Snowflake）保证分布式环境下的唯一性，
     * 前缀为日期时间便于人工识别订单创建时间。
     * </p>
     */
    private String generateOrderSn() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.getSnowflake(1, 1).nextIdStr().substring(11);
    }

    /**
     * 发送订单超时取消延时消息到 TTL 队列
     */
    private void sendTimeoutMessage(Integer orderId) {
        if (rabbitTemplate == null) return;
        OrderTimeoutMessage msg = new OrderTimeoutMessage();
        msg.setOrderId(orderId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_DELAY_QUEUE, msg);
    }

    /** 缓存订单商品快照到 Redis，避免 adjustSalesRank 查 DB */
    private void cacheOrderItems(Integer orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                        .eq("order_id", orderId));
        if (items.isEmpty()) return;
        List<Map<String, Object>> snapshot = new ArrayList<>();
        for (OrderItem item : items) {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", item.getProductId());
            m.put("amount", item.getAmount());
            snapshot.add(m);
        }
        redisUtil.set(RedisConstants.ORDER_ITEMS_SNAPSHOT_KEY + orderId, JSONUtil.toJsonStr(snapshot), 3600);
    }

    private void adjustSalesRank(Integer orderId, int delta) {
        String json = (String) redisUtil.get(RedisConstants.ORDER_ITEMS_SNAPSHOT_KEY + orderId);
        if (json == null) {
            // 兜底：缓存未命中时查 DB
            List<OrderItem> items = orderItemMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                            .eq("order_id", orderId));
            for (OrderItem item : items) {
                if (delta > 0) {
                    salesRankService.recordSale(item.getProductId(), item.getAmount());
                } else {
                    salesRankService.recordCancel(item.getProductId(), item.getAmount());
                }
            }
            return;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        List<Map<String, Object>> snapshot = (List) JSONUtil.toList(json, Map.class);
        for (Map<String, Object> item : snapshot) {
            int productId = (int) item.get("productId");
            int amount = (int) item.get("amount");
            if (delta > 0) {
                salesRankService.recordSale(productId, amount);
            } else {
                salesRankService.recordCancel(productId, amount);
            }
        }
    }
}
