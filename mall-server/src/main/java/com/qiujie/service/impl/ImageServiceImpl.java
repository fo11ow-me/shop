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

    @Value("${upload.path:D:/project/idea/mall/file/}")
    private String uploadPath;

    public ImageServiceImpl(OssService ossService) {
        this.ossService = ossService;
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
        Path localPath = Paths.get(uploadPath, fileName).normalize();
        Path basePath = Paths.get(uploadPath).normalize();
        Path productPath = Paths.get(uploadPath, "product").normalize();
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
