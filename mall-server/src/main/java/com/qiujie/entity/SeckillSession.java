package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "商品ID不能为空")
    @TableField("product_id")
    private Integer productId;

    @NotNull(message = "秒杀价格不能为空")
    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @NotNull(message = "秒杀库存不能为空")
    @TableField("seckill_stock")
    private Integer seckillStock;

    @NotNull(message = "开始时间不能为空")
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
