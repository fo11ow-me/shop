package com.qiujie.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface OssService {

    String upload(MultipartFile file, String dir) throws IOException;

    String upload(InputStream inputStream, String fileName);

    String getUrl(String fileName, long expireMs);

    String getUrl(String fileName);

    void delete(String fileName);

    void deleteBatch(List<String> fileNames);

    String getETag(String fileName);

    byte[] download(String fileName) throws IOException;
}
