package com.qiujie.mq;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 秒杀下单消息体
 *
 * @author qiujie
 */
@Data
public class SeckillMessage {
    private Integer sessionId;
    private Integer userId;
    private Integer productId;
    private BigDecimal seckillPrice;
}
