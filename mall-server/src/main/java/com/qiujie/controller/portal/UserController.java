package com.qiujie.controller.portal;

import cn.hutool.core.util.StrUtil;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.entity.User;
import com.qiujie.service.OssService;
import com.qiujie.service.UserService;
import com.qiujie.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController("portalUserController")
@RequestMapping("/portal/user")
@Tag(name = "门户端-用户")
public class UserController {

    private final UserService userService;
    private final OssService ossService;

    public UserController(UserService userService, OssService ossService) {
        this.userService = userService;
        this.ossService = ossService;
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/info")
    public ResponseDTO<User> info() {
        return Response.success(userService.queryInfo(SecurityUtil.getCurrentUserId()));
    }

    @Operation(summary = "获取用户头像")
    @GetMapping("/avatar")
    public void avatar(HttpServletResponse response) throws IOException {
        User user = userService.getById(SecurityUtil.getCurrentUserId());
        if (user == null || StrUtil.isBlank(user.getAvatar())) {
            response.setStatus(404);
            return;
        }
        String ossKey = "avatar/" + user.getAvatar();
        String ext = "";
        int dot = ossKey.lastIndexOf('.');
        if (dot > 0) ext = ossKey.substring(dot + 1).toLowerCase();
        String contentType = switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
        byte[] bytes = ossService.download(ossKey);
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "private, max-age=300");
        response.getOutputStream().write(bytes);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    public ResponseDTO<User> update(@RequestBody Map<String, String> params) {
        return Response.success(userService.updateInfo(SecurityUtil.getCurrentUserId(), params));
    }

    @Operation(summary = "更新头像")
    @PostMapping("/avatar")
    public ResponseDTO<Map<String, String>> updateAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Response.error(BusinessStatusEnum.FILE_EMPTY);
        }
        return Response.success(userService.updateAvatar(SecurityUtil.getCurrentUserId(), file));
    }
}
