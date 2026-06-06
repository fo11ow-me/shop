package com.qiujie.enums;

import lombok.Getter;

/**
 * 秒杀结果枚举
 *
 * @author qiujie
 */
@Getter
public enum SeckillResultEnum {

    QUEUING(0, "排队中"),
    SUCCESS(1, "秒杀成功"),
    FAILED(-1, "秒杀失败");

    private final int code;
    private final String msg;

    SeckillResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
