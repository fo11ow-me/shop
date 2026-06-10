package com.qiujie.controller.portal;

import com.qiujie.annotation.RateLimit;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@RestController("portalAuthController")
@RequestMapping("/portal/auth")
@Tag(name = "门户端-认证")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @RateLimit(key = "rate:captcha:portal:", limit = 10, window = 60)
    @Operation(summary = "获取验证码")
    @GetMapping("/verificationCode")
    public void getVerificationCode(HttpServletResponse response) throws IOException {
        Map<String, Object> result = authService.getVerificationCode();
        response.setHeader("X-Verification-Uuid", (String) result.get("uuid"));
        ImageIO.write((BufferedImage) result.get("image"), "jpeg", response.getOutputStream());
    }

    @RateLimit(key = "rate:register:", limit = 3, window = 60, message = "注册过于频繁，请1分钟后再试")
    @Operation(summary = "注册")
    @PostMapping("/register")
    public ResponseDTO<Void> register(@RequestBody Map<String, String> params) {
        authService.register(params.get("code"), params.get("password"), params.get("phone"),
                params.get("verificationCode"), params.get("uuid"));
        return Response.ok("注册成功");
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseDTO<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token, "/portal");
        return Response.ok("已退出");
    }

    @RateLimit(key = "rate:login:portal:", limit = 5, window = 60, message = "登录过于频繁，请1分钟后再试")
    @Operation(summary = "登录")
    @PostMapping("/login")
    public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        return Response.success(authService.login(
            params.get("code"), params.get("password"),
            params.get("verificationCode"), params.get("uuid"), "/portal"));
    }
}
