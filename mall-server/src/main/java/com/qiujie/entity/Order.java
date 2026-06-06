package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qiujie.enums.OrderStatusEnum;
import com.qiujie.enums.PayMethodEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName("oms_order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("address_id")
    private Integer addressId;

    @TableField("order_sn")
    private String orderSn;

    @TableField("payment_sn")
    private String paymentSn;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("pay_method")
    private PayMethodEnum payMethod;

    @TableField("express_delivery")
    private Integer expressDelivery;

    @TableField("status")
    private OrderStatusEnum status;

    @TableField("recipient_name")
    private String recipientName;

    @TableField("recipient_phone")
    private String recipientPhone;

    @TableField("recipient_address")
    private String recipientAddress;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("delivery_time")
    private LocalDateTime deliveryTime;

    @TableField("receipt_time")
    private LocalDateTime receiptTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String userName;

    @TableField(exist = false)
    private List<OrderItem> items;
}
