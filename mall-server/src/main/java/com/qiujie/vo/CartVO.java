package com.qiujie.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CartVO {

    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer amount;
    private Integer isSelected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String productName;
    private BigDecimal productPrice;
    private String productImg;
    private Integer productStock;
}
