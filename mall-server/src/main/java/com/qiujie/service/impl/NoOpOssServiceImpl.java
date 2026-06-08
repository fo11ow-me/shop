package com.qiujie.service.impl;

import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.service.OssService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * OSS 未配置时的降级实现，所有操作抛出异常由调用方兜底
 */
@Service
@ConditionalOnExpression("'${aliyun.oss.access-key-id:}'.isEmpty()")
public class NoOpOssServiceImpl implements OssService {

    @Override
    public String upload(MultipartFile file, String dir) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public String upload(InputStream inputStream, String fileName) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public String getUrl(String fileName, long expireMs) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public String getUrl(String fileName) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public void delete(String fileName) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public void deleteBatch(List<String> fileNames) {
        throw new ServiceException(BusinessStatusEnum.ERROR);
    }

    @Override
    public String getETag(String fileName) {
        return null;
    }

    @Override
    public byte[] download(String fileName) throws IOException {
        throw new IOException("OSS not configured");
    }
}
