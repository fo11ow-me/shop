package com.qiujie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum implements BaseEnum<Integer> {

    USER(0, "用户"),
    ADMIN(1, "管理员");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String message;
}
