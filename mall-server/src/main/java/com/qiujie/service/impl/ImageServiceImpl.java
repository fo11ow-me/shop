package com.qiujie.service.impl;

import com.qiujie.service.ImageService;
import com.qiujie.service.OssService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author qiujie
 */
@Service
public class ImageServiceImpl implements ImageService {

    private final OssService ossService;

    @Value("${upload.path:img/}")
    private String uploadPath;

    public ImageServiceImpl(OssService ossService) {
        this.ossService = ossService;
    }

    private Path resolveBasePath() {
        Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            // 如果是相对路径且以 mall-server 结尾的工作目录，向上寻找工程根目录
            Path currentDir = Paths.get("").toAbsolutePath();
            if (currentDir.endsWith("mall-server") && !Files.exists(currentDir.resolve(path))) {
                Path parentPath = currentDir.getParent().resolve(path);
                if (Files.exists(parentPath)) {
                    return parentPath.normalize();
                }
            }
            return currentDir.resolve(path).normalize();
        }
        return path.normalize();
    }

    @Override
    public void serveProductImage(String key, HttpServletResponse response) throws IOException {
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
        Path basePath = resolveBasePath();
        Path localPath = basePath.resolve(fileName).normalize();
        Path productPath = basePath.resolve("product").normalize();
        if (!localPath.startsWith(basePath) || !localPath.startsWith(productPath)) {
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
                    // 本地缓存写入失败不影响返回
                }
            } catch (Exception e) {
                Path defaultPath = basePath.resolve("product/default.png");
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
