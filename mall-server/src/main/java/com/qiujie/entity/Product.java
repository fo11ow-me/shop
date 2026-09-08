package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.qiujie.enums.ProductStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName("pms_product")
public class Product {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "商品名称不能为空")
    @TableField("name")
    private String name;

    @NotNull(message = "商品价格不能为空")
    @TableField("price")
    private BigDecimal price;

    @NotNull(message = "商品库存不能为空")
    @TableField("stock")
    private Integer stock;

    @NotNull(message = "商品分类不能为空")
    @TableField("category_id")
    private Integer categoryId;

    @TableField("detail")
    private String detail;

    @TableField("status")
    private ProductStatusEnum status;

    @TableField("version")
    private Integer version;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<ProductImg> images;

    @TableField(exist = false)
    private String categoryName;
}
