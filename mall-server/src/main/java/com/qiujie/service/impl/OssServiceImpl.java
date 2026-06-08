package com.qiujie.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.qiujie.config.OssConfig;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.service.OssService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@ConditionalOnExpression("!'${aliyun.oss.access-key-id:}'.isEmpty()")
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    public OssServiceImpl(OSS ossClient, OssConfig ossConfig) {
        this.ossClient = ossClient;
        this.ossConfig = ossConfig;
    }

    @Override
    public String upload(MultipartFile file, String dir) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        Set<String> allowedExts = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
        if (!allowedExts.contains(ext.toLowerCase())) {
            throw new ServiceException(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED);
        }
        String fileName = (StrUtil.isNotBlank(dir) ? dir + "/" : "") + IdUtil.simpleUUID() + ext;
        return upload(file.getInputStream(), fileName);
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        ossClient.putObject(ossConfig.getBucketName(), fileName, inputStream);
        return fileName;
    }

    @Override
    public String getUrl(String fileName, long expireMs) {
        long expire = expireMs > 0 ? expireMs : 3600_000;
        Date expiration = new Date(System.currentTimeMillis() + expire);
        URL url = ossClient.generatePresignedUrl(ossConfig.getBucketName(), fileName, expiration);
        return url.toString();
    }

    @Override
    public String getUrl(String fileName) {
        return getUrl(fileName, 3600_000);
    }

    @Override
    public void delete(String fileName) {
        ossClient.deleteObject(ossConfig.getBucketName(), fileName);
    }

    @Override
    public void deleteBatch(List<String> fileNames) {
        ossClient.deleteObjects(new DeleteObjectsRequest(ossConfig.getBucketName()).withKeys(fileNames));
    }

    @Override
    public String getETag(String fileName) {
        try {
            return ossClient.getSimplifiedObjectMeta(ossConfig.getBucketName(), fileName).getETag();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public byte[] download(String fileName) throws IOException {
        try (InputStream is = ossClient.getObject(ossConfig.getBucketName(), fileName).getObjectContent()) {
            return is.readAllBytes();
        }
    }
}
