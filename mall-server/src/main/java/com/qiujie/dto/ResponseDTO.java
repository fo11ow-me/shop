package com.qiujie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应体")
public class ResponseDTO<T> {

    @Schema(description = "状态码")
    private Integer code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(200, "成功", data);
    }

    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>(200, "成功", null);
    }

    public static <T> ResponseDTO<T> error(String message) {
        return new ResponseDTO<>(300, message, null);
    }

    public static <T> ResponseDTO<T> error(Integer code, String message) {
        return new ResponseDTO<>(code, message, null);
    }
}
