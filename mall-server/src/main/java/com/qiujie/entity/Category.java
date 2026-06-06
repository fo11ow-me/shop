package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)

@TableName("pms_category")
public class Category {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    private String name;


    private Integer parentId;


    private LocalDateTime createTime;


    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private List<Category> children;
}
