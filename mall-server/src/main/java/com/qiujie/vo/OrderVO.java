package com.qiujie.vo;

import com.qiujie.entity.OrderItem;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.enums.PayMethodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class OrderVO {

    private Integer id;
    private Integer userId;
    private Integer addressId;
    private String orderSn;
    private String paymentSn;
    private BigDecimal totalAmount;
    private PayMethodEnum payMethod;
    private Integer expressDelivery;
    private OrderStatusEnum status;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiptTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String userName;
    private List<OrderItem> items;
}
