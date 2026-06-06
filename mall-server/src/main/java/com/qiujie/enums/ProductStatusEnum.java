package com.qiujie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatusEnum implements BaseEnum<Integer> {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String message;
}
