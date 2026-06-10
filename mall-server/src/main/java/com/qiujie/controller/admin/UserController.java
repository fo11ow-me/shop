package com.qiujie.controller.admin;


import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import cn.hutool.core.util.StrUtil;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.User;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.service.OssService;
import com.qiujie.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理端-用户管理")
public class UserController {

    private final UserService userService;
    private final OssService ossService;

    public UserController(UserService userService, OssService ossService) {
        this.userService = userService;
        this.ossService = ossService;
    }

    @Operation(summary = "新增")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Void> add(@Valid @RequestBody User user) {
        userService.add(user);
        return Response.success();
    }

    @Operation(summary = "逻辑删除")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return Response.success();
    }

    @Operation(summary = "批量逻辑删除")
    @DeleteMapping("/batch/{ids}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Void> deleteBatch(@PathVariable List<Integer> ids) {
        userService.deleteBatch(ids);
        return Response.success();
    }

    @Operation(summary = "编辑更新")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Void> edit(@Valid @RequestBody User user) {
        userService.edit(user);
        return Response.success();
    }

    @Operation(summary = "查询")
    @GetMapping("/{id}")
    public ResponseDTO<User> query(@PathVariable Integer id) {
        return Response.success(userService.query(id));
    }

    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/code/{code}")
    public ResponseDTO<User> queryByCode(@PathVariable String code) {
        return Response.success(userService.queryByCode(code));
    }

    @Operation(summary = "查询员工信息")
    @GetMapping("/info/{id}")
    public ResponseDTO<User> queryInfo(@PathVariable Integer id) {
        return Response.success(userService.queryInfo(id));
    }

    @Operation(summary = "多条件分页查询")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer current, @RequestParam(defaultValue = "10") Integer size, String name, String birthday, Integer status, String code, String phone, Integer gender, String startTime, String endTime) {
        return Response.success(userService.list(current, size, name, birthday, status, code, phone, gender, startTime, endTime));
    }

    @Operation(summary = "数据导出接口")
    @GetMapping("/export/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    public void export(HttpServletResponse response, @PathVariable String filename) throws IOException {
        userService.export(response, filename);
    }

    @Operation(summary = "数据导入接口")
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseDTO<Void> imp(MultipartFile file) throws IOException {
        userService.imp(file);
        return Response.success();
    }

    @Operation(summary = "检查员工的密码")
    @GetMapping("/{pwd}/{id}")
    public ResponseDTO<Void> validate(@PathVariable String pwd, @PathVariable Integer id) {
        userService.validate(pwd, id);
        return Response.success();
    }

    @Operation(summary = "获取头像")
    @GetMapping("/avatar/{id}")
    public void avatar(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        User user = userService.getById(id);
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

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public ResponseDTO<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) return Response.error(BusinessStatusEnum.FILE_EMPTY);
        String ossKey = ossService.upload(file, "avatar");
        String fileName = ossKey.startsWith("avatar/") ? ossKey.substring(7) : ossKey;
        String url = ossService.getUrl(ossKey);
        return Response.success(Map.of("fileName", fileName, "url", url));
    }

    @Operation(summary = "更新密码")
    @PutMapping("/reset")
    public ResponseDTO<Void> reset(@Valid @RequestBody User user) {
        userService.reset(user);
        return Response.success();
    }
}
