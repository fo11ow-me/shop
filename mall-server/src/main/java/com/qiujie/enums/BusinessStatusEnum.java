package com.qiujie.enums;

import lombok.Getter;

@Getter
public enum BusinessStatusEnum implements BaseEnum {

    SUCCESS(200, "成功"),
    ERROR(300, "失败"),
    STAFF_NOT_EXIST(400, "没有此用户，请重新登录"),
    STAFF_STATUS_ERROR(500, "状态异常，请联系管理员"),
    FILE_NOT_EXIST(600, "文件不存在"),
    FILE_READ_ERROR(700, "文件读取失败"),
    FILE_WRITE_ERROR(800, "文件写入失败"),
    FILE_UPLOAD_ERROR(900, "文件上传失败"),
    FILE_EMPTY(901, "文件不能为空"),
    FILE_TYPE_NOT_ALLOWED(902, "文件类型不允许，仅支持 jpg/jpeg/png/gif/webp/bmp 格式"),
    DATA_IMPORT_ERROR(1000, "数据导入失败"),
    BATCH_DELETE_ERROR(1100, "批量删除失败"),
    UNAUTHORIZED(1200, "认证失败，请重新登录"),
    FORBIDDEN(1300, "你没有此权限"),

    // 用户/认证
    AUTH_EMPTY_CREDENTIALS(1400, "用户名和密码不能为空"),
    USERNAME_EXISTS(1401, "用户名已存在"),
    AUTH_BAD_CREDENTIALS(1402, "用户名或密码错误"),
    ACCOUNT_DISABLED(1403, "账号已被禁用"),
    CAPTCHA_NOT_EXIST(1404, "验证码不存在"),
    CAPTCHA_EXPIRED(1405, "验证码已过期"),
    CAPTCHA_ERROR(1406, "验证码错误"),
    PASSWORD_ERROR(1407, "密码错误"),
    USER_NOT_EXIST(1408, "用户不存在"),
    TOKEN_BLACKLISTED(1409, "令牌已失效，请重新登录"),

    // 商品/购物车
    PRODUCT_NOT_EXIST(1500, "商品不存在"),
    PRODUCT_STOCK_INSUFFICIENT(1501, "商品库存不足"),
    CART_NOT_EXIST(1502, "购物车记录不存在"),
    CART_SELECT_EMPTY(1503, "请选择要删除的商品"),

    // 订单
    ORDER_NOT_EXIST(1600, "订单不存在"),
    ORDER_STATUS_ERROR(1601, "订单状态不正确"),
    ORDER_CANCEL_DENIED(1602, "只有待支付订单可以取消"),
    ORDER_EMPTY(1603, "请选择要购买的商品"),
    PARAM_ERROR(1604, "参数错误"),
    ORDER_IN_PROGRESS(1605, "您的订单正在处理中，请勿重复提交"),
    ADDRESS_NOT_EXIST(1610, "请先添加收货地址"),

    // 限流
    RATE_LIMIT_EXCEEDED(1700, "请求过于频繁，请稍后再试"),

    // 秒杀
    LOCK_FAILED(1799, "操作频繁，请稍后再试"),
    SECKILL_SESSION_NOT_EXIST(1800, "秒杀活动不存在"),
    SECKILL_SESSION_EXPIRED(1801, "秒杀活动已结束"),
    SECKILL_STOCK_EMPTY(1802, "已抢光"),
    SECKILL_DUPLICATE(1803, "您已参与过此秒杀活动"),
    SECKILL_ORDER_FAILED(1804, "秒杀下单失败");

    private final Integer code;
    private final String message;

    BusinessStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
