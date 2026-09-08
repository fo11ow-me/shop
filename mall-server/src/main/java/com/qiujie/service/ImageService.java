package com.qiujie.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 商品图片服务 — 封装本地文件读取、OSS 回源下载与 HTTP 响应输出。
 *
 * @author qiujie
 */
public interface ImageService {

    /**
     * 根据 key 获取商品图片并直接写入 HTTP 响应。
     *
     * @param key      图片键（自动补 product/ 前缀）
     * @param response HTTP 响应
     */
    void serveProductImage(String key, HttpServletResponse response) throws IOException;
}
