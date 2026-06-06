package com.qiujie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum implements BaseEnum<Integer> {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String message;
}
