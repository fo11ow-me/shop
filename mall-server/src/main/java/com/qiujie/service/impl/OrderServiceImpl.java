package com.qiujie.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiujie.entity.*;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.enums.PayMethodEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.mapper.*;
import com.qiujie.vo.CartVO;
import com.qiujie.vo.OrderVO;
import com.qiujie.service.OrderService;
import com.qiujie.util.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartMapper cartMapper, ProductMapper productMapper,
                            ProductImgMapper productImgMapper, RedisUtil redisUtil) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
        this.productImgMapper = productImgMapper;
        this.redisUtil = redisUtil;
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
        List<CartVO> cartList = cartMapper.selectByUserId(userId);
        List<CartVO> selectedCarts = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Integer> cartIds = (List<Integer>) params.get("cartIds");
        if (cartIds != null && !cartIds.isEmpty()) {
            for (CartVO cart : cartList) {
                if (cartIds.contains(cart.getId())) {
                    selectedCarts.add(cart);
                }
            }
        } else {
            for (CartVO cart : cartList) {
                if (cart.getIsSelected() != null && cart.getIsSelected() == 1) {
                    selectedCarts.add(cart);
                }
            }
        }
        if (selectedCarts.isEmpty()) {
            throw new ServiceException(BusinessStatusEnum.ORDER_EMPTY);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartVO cart : selectedCarts) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStock() < cart.getAmount()) {
                throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT.getCode(), "商品[" + cart.getProductName() + "]库存不足");
            }
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cart.getAmount()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductPrice(product.getPrice());
            item.setProductImg(cart.getProductImg());
            item.setAmount(cart.getAmount());
            orderItems.add(item);

            product.setStock(product.getStock() - cart.getAmount());
            productMapper.updateById(product);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setOrderSn(generateOrderSn());
        order.setTotalAmount(totalAmount);
        order.setPayMethod(payMethod);
        order.setStatus(OrderStatusEnum.PENDING_PAY);
        order.setRecipientName((String) params.getOrDefault("recipientName", ""));
        order.setRecipientPhone((String) params.getOrDefault("recipientPhone", ""));
        order.setRecipientAddress((String) params.getOrDefault("recipientAddress", ""));
        Integer expressDelivery = (Integer) params.getOrDefault("expressDelivery", 0);
        order.setExpressDelivery(expressDelivery);
        save(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        for (CartVO cart : selectedCarts) {
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

        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < amount) {
            throw new ServiceException(BusinessStatusEnum.PRODUCT_STOCK_INSUFFICIENT);
        }

        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(amount));

        Order order = new Order();
        order.setUserId(userId);
        order.setOrderSn(generateOrderSn());
        order.setTotalAmount(totalAmount);
        order.setPayMethod(PayMethodEnum.UNKNOWN);
        order.setStatus(OrderStatusEnum.PENDING_PAY);
        order.setRecipientName((String) params.getOrDefault("recipientName", ""));
        order.setRecipientPhone((String) params.getOrDefault("recipientPhone", ""));
        order.setRecipientAddress((String) params.getOrDefault("recipientAddress", ""));
        Integer expressDelivery = (Integer) params.getOrDefault("expressDelivery", 0);
        order.setExpressDelivery(expressDelivery);
        save(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductPrice(product.getPrice());
        item.setAmount(amount);
        ProductImg firstImg = productImgMapper.selectFirstByProductId(product.getId());
        if (firstImg != null) item.setProductImg(firstImg.getUrl());
        orderItemMapper.insert(item);

        product.setStock(product.getStock() - amount);
        productMapper.updateById(product);

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
    }

    public List<OrderVO> list(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    public List<OrderVO> listByStatus(Integer userId, Integer status) {
        List<OrderVO> all = orderMapper.selectByUserId(userId);
        return all.stream().filter(o -> o.getStatus() != null && o.getStatus().getCode() == status).toList();
    }

    public OrderVO detail(Integer userId, Integer id) {
        OrderVO order = orderMapper.selectDetailById(id);
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

    public IPage<OrderVO> list(Integer current, Integer size, String orderSn, String userName,
                                String status, String startTime, String endTime) {
        Page<OrderVO> page = new Page<>(current, size);
        List<Integer> statusList = null;
        if (status != null && !status.isEmpty()) {
            statusList = Arrays.stream(status.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return orderMapper.selectPageWithParams(page, orderSn, userName, statusList, startTime, endTime);
    }

    public OrderVO detail(Integer id) {
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
        for (Order order : orders) {
            if (order.getStatus() == OrderStatusEnum.PAID) {
                order.setStatus(OrderStatusEnum.SHIPPED);
                updateById(order);
            }
        }
    }

    public void delete(Integer id) {
        removeById(id);
    }

    public void batchDelete(List<Integer> ids) {
        removeBatchByIds(ids);
    }

    private String generateOrderSn() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));
    }
}
