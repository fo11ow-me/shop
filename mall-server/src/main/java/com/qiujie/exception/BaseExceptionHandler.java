package com.qiujie.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.enums.BusinessStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

@ControllerAdvice
public class BaseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleNotLogin(NotLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Response.error(BusinessStatusEnum.UNAUTHORIZED));
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleNotPermission(NotPermissionException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Response.error(BusinessStatusEnum.FORBIDDEN));
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseBody
    public ResponseEntity<ResponseDTO> handleNotRole(NotRoleException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Response.error(BusinessStatusEnum.FORBIDDEN));
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseDTO handle(ServiceException exception){
        logger.info(exception.getMessage());
        return Response.error(exception.getCode(),exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseDTO handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Response.error(400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseDTO handleIllegalArgument(IllegalArgumentException e) {
        logger.error("参数错误", e);
        return Response.error(400, "请求参数不合法");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseDTO handleException(Exception e) {
        logger.error("未处理异常", e);
        return Response.error(500, "服务器内部错误");
    }
}
