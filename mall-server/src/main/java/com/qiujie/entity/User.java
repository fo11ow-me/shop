package com.qiujie.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qiujie.annotation.ExcelColumn;
import com.qiujie.enums.GenderEnum;
import com.qiujie.enums.RoleEnum;
import com.qiujie.enums.UserStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Accessors(chain = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "用户编码不能为空")
    @ExcelColumn("编码")
    @TableField("code")
    private String code;

    @NotBlank(message = "用户姓名不能为空")
    @ExcelColumn("姓名")
    @TableField("name")
    private String name;

    @TableField("gender")
    private GenderEnum gender;

    @ExcelColumn("地址")
    @TableField("address")
    private String address;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField("pwd")
    private String password;

    @TableField("avatar")
    private String avatar;

    @ExcelColumn("生日")
    @TableField("birthday")
    private Date birthday;

    @ExcelColumn("电话")
    @TableField("phone")
    private String phone;

    @ExcelColumn("邮箱")
    @TableField("email")
    private String email;

    @ExcelColumn("备注")
    @TableField("remark")
    private String remark;

    @TableField("role")
    private RoleEnum role;

    @TableField("status")
    private UserStatusEnum status;

    @ExcelColumn("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("create_time")
    private Timestamp createTime;

    @ExcelColumn("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("update_time")
    private Timestamp updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Integer deleted;
}
