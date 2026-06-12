package com.qiujie.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Product;
import com.qiujie.service.OssService;
import com.qiujie.service.ProductService;
import com.qiujie.util.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.qiujie.constants.RedisConstants.*;

@SaCheckRole("admin")
@RestController("adminProductController")
@RequestMapping("/admin/product")
@Tag(name = "管理端-商品管理")
public class ProductController {

    private final ProductService productService;
    private final OssService ossService;
    private final RedisUtil redisUtil;

    @Value("${upload.path:D:/project/idea/mall/file/}")
    private String uploadPath;

    public ProductController(ProductService productService, OssService ossService, RedisUtil redisUtil) {
        this.productService = productService;
        this.ossService = ossService;
        this.redisUtil = redisUtil;
    }

    @Operation(summary = "获取商品图片")
    @GetMapping("/img")
    public void image(@RequestParam("key") String key, HttpServletResponse response) throws IOException {
        String fileName = key.contains("/") ? key : "product/" + key;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) ext = fileName.substring(dot + 1).toLowerCase();
        String contentType = switch (ext) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
        byte[] bytes;
        Path localPath = Paths.get(uploadPath, fileName).normalize();
        if (!localPath.startsWith(Paths.get(uploadPath).normalize())) {
            response.setStatus(404);
            return;
        }
        if (Files.exists(localPath)) {
            bytes = Files.readAllBytes(localPath);
        } else {
            try {
                bytes = ossService.download(fileName);
                try {
                    Files.createDirectories(localPath.getParent());
                    Files.write(localPath, bytes);
                } catch (Exception ignored) {
                    // 缓存写入失败不影响图片返回
                }
            }
            catch (Exception e) {
                Path defaultPath = Paths.get(uploadPath, "product/default.png");
                if (Files.exists(defaultPath)) {
                    bytes = Files.readAllBytes(defaultPath);
                    contentType = "image/png";
                } else {
                    bytes = new byte[0];
                }
            }
        }
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "no-cache");
        response.getOutputStream().write(bytes);
    }

    @Operation(summary = "商品分页列表")
    @GetMapping("/list")
    public ResponseDTO<IPage<Product>> list(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             String name, Integer status, Integer categoryId) {
        return Response.success(productService.listPage(current, size, name, status, categoryId));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ResponseDTO<Void> add(@Valid @RequestBody Product product) {
        productService.add(product);
        redisUtil.del(CACHE_HOME_KEY);
        return Response.ok("新增成功");
    }

    @Operation(summary = "修改商品")
    @PutMapping
    public ResponseDTO<Void> edit(@Valid @RequestBody Product product) {
        productService.updateById(product);
        redisUtil.del(CACHE_HOME_KEY);
        return Response.ok("修改成功");
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Integer id) {
        productService.removeById(id);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + id);
        return Response.ok("删除成功");
    }

    @Operation(summary = "更新商品状态")
    @PutMapping("/status/{id}")
    public ResponseDTO<Void> updateStatus(@PathVariable Integer id) {
        productService.toggleStatus(id);
        redisUtil.del(CACHE_HOME_KEY);
        redisUtil.del(CACHE_PRODUCT_KEY + id);
        return Response.ok("操作成功");
    }
}
