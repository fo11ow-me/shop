package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("oms_order_item")
public class OrderItem {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    
    private Integer orderId;

    
    private Integer productId;

    
    private String productName;

    
    private BigDecimal productPrice;

    
    private String productImg;

    
    private Integer amount;

    
    private LocalDateTime createTime;

    
    private LocalDateTime updateTime;

    
    @TableField("is_deleted")
    private Integer deleted;
}
