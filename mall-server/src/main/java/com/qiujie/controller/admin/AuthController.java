package com.qiujie.controller.admin;

import com.qiujie.annotation.RateLimit;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

@RestController("adminAuthController")
@RequestMapping("/admin/auth")
@Tag(name = "管理端-认证")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseDTO<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token, "/admin");
        return Response.ok("已退出");
    }

    @RateLimit(key = "rate:login:admin:", limit = 5, window = 60)
    @Operation(summary = "登录")
    @PostMapping("/login")
    public ResponseDTO<Map<String, Object>> login(@RequestBody Map<String, String> params,
                                                  HttpServletRequest request) {
        return Response.success(authService.login(
            params.get("code"), params.get("password"),
            params.get("verificationCode"), params.get("uuid"), "/admin", request));
    }

    @RateLimit(key = "rate:captcha:admin:", limit = 10, window = 60)
    @Operation(summary = "获取验证码")
    @GetMapping("/verificationCode")
    public void getVerificationCode(HttpServletResponse response) throws IOException {
        Map<String, Object> result = authService.getVerificationCode();
        response.setHeader("X-Verification-Uuid", (String) result.get("uuid"));
        ImageIO.write((BufferedImage) result.get("image"), "jpeg", response.getOutputStream());
    }
}
