package com.qiujie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiujie.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface AuthService extends IService<User> {

    void register(String code, String password, String phone, String verificationCode, String uuid);

    Map<String, Object> login(String code, String password, String verificationCode, String uuid, String pathPrefix, HttpServletRequest request);

    /**
     * 生成验证码图片和 UUID，存入 Redis，返回 UUID 和 BufferedImage
     *
     * @return Map 包含 "uuid" (String) 和 "image" (BufferedImage)
     */
    Map<String, Object> getVerificationCode();

    void logout(String token, String requestUri);
}
