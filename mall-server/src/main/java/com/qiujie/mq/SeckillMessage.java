package com.qiujie.mq;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀下单消息体
 *
 * @author qiujie
 */
@Data
public class SeckillMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer sessionId;
    private Integer userId;
    private Integer productId;
    private BigDecimal seckillPrice;
}
