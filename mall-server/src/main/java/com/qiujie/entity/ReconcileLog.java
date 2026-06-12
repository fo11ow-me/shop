package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀库存对账日志 — 每次库存变更记录一条，用于事后排查不一致
 *
 * @author qiujie
 */
@Data
@TableName("reconcile_log")
public class ReconcileLog {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("seckill_id")
    private Integer seckillId;

    @TableField("user_id")
    private Integer userId;

    /** 操作类型：DEDUCT=扣减, ROLLBACK=回滚 */
    @TableField("operation")
    private String operation;

    @TableField("stock_before")
    private Integer stockBefore;

    @TableField("stock_after")
    private Integer stockAfter;

    @TableField("create_time")
    private LocalDateTime createTime;
}
