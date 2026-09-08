package com.qiujie.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PayMethodEnum implements BaseEnum<Integer> {

    UNKNOWN(0, "未知"),
    WECHAT(1, "微信"),
    ALIPAY(2, "支付宝"),
    BANK_CARD(3, "银行卡");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String message;
}
