package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)

@TableName("pms_product_img")
public class ProductImg {

    private static final long serialVersionUID = 1L;

    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    
    private String url;

    
    private Integer productId;

    
    private LocalDateTime createTime;

    
    private LocalDateTime updateTime;

    
    
    @TableField("is_deleted")
    private Integer deleted;
}
