package com.qiujie.controller.portal;

import com.qiujie.dto.Response;
import com.qiujie.dto.ResponseDTO;
import com.qiujie.entity.Category;
import com.qiujie.service.OssService;
import com.qiujie.service.ProductService;
import com.qiujie.vo.ProductVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController("portalProductController")
@RequestMapping("/portal/product")
public class ProductController {

    private final ProductService productService;
    private final OssService ossService;

    @Value("${upload.path:D:/project/idea/mall/file/}")
    private String uploadPath;

    public ProductController(ProductService productService, OssService ossService) {
        this.productService = productService;
        this.ossService = ossService;
    }

    @GetMapping("/home")
    public ResponseDTO<List<Map<String, Object>>> home() {
        return Response.success(productService.home());
    }

    @GetMapping("/categories")
    public ResponseDTO<List<Category>> categories() {
        return Response.success(productService.categories());
    }

    @GetMapping("/category/{id}")
    public ResponseDTO<Map<String, Object>> getByCategory(@PathVariable Integer id,
                                                           @RequestParam(defaultValue = "1") Integer current,
                                                           @RequestParam(defaultValue = "10") Integer size) {
        return Response.success(productService.getByCategory(id, current, size));
    }

    @GetMapping("/search")
    public ResponseDTO<Map<String, Object>> search(@RequestParam String keyword,
                                                    @RequestParam(defaultValue = "1") Integer current,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        var result = productService.search(keyword, current, size);
        if (result == null) {
            return Response.success();
        }
        return Response.success(result);
    }

    @GetMapping("/detail/{id}")
    public ResponseDTO<ProductVO> detail(@PathVariable Integer id) {
        return Response.success(productService.detail(id));
    }

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
        Path localPath = Paths.get(uploadPath, fileName);
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

}
