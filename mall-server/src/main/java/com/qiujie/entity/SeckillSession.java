package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动场次实体
 *
 * @author qiujie
 */
@Data
@Accessors(chain = true)
@TableName("sms_seckill_session")
public class SeckillSession {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("product_id")
    private Integer productId;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("seckill_stock")
    private Integer seckillStock;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private String productImg;

    @TableField(exist = false)
    private BigDecimal productPrice;
}
