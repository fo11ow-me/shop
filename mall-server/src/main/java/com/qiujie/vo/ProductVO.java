package com.qiujie.vo;

import com.qiujie.entity.ProductImg;
import com.qiujie.enums.ProductStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ProductVO {

    private Integer id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer categoryId;
    private String detail;
    private ProductStatusEnum status;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String categoryName;
    private List<ProductImg> images;
}
