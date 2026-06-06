package com.qiujie.service;

import com.aliyun.oss.OSS;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OssServiceImpl tests")
class OssServiceImplTest {

    @Autowired
    private OssService ossService;

    /**
     * Mock the OSS client so that no real network calls are made.
     * The mock replaces the real OSS bean created by {@link com.qiujie.config.OssConfig#ossClient()}.
     */
    @MockBean
    private OSS ossClient;

    // ==================== Bean loading ====================

    @Test
    @DisplayName("service bean loads correctly in test context")
    void shouldLoadBean() {
        assertNotNull(ossService);
    }

    // ==================== Valid file type acceptance ====================

    @Test
    @DisplayName("upload — accepts .jpg")
    void shouldAcceptJpg() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"), "result should end with .jpg");
    }

    @Test
    @DisplayName("upload — accepts .jpeg")
    void shouldAcceptJpeg() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpeg", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".jpeg"), "result should end with .jpeg");
    }

    @Test
    @DisplayName("upload — accepts .png")
    void shouldAcceptPng() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".png"), "result should end with .png");
    }

    @Test
    @DisplayName("upload — accepts .gif")
    void shouldAcceptGif() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.gif", "image/gif", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".gif"), "result should end with .gif");
    }

    @Test
    @DisplayName("upload — accepts .webp")
    void shouldAcceptWebp() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".webp"), "result should end with .webp");
    }

    @Test
    @DisplayName("upload — accepts .bmp")
    void shouldAcceptBmp() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.bmp", "image/bmp", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".bmp"), "result should end with .bmp");
    }

    // ==================== Invalid file type rejection ====================

    @Test
    @DisplayName("upload — rejects .txt")
    void shouldRejectTxt() {
        MultipartFile file = new MockMultipartFile("file", "document.txt", "text/plain", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "docs"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects .exe")
    void shouldRejectExe() {
        MultipartFile file = new MockMultipartFile("file", "app.exe", "application/octet-stream", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "apps"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects .jsp")
    void shouldRejectJsp() {
        MultipartFile file = new MockMultipartFile("file", "malicious.jsp", "application/x-jsp", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "web"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects .html")
    void shouldRejectHtml() {
        MultipartFile file = new MockMultipartFile("file", "page.html", "text/html", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "web"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects .pdf")
    void shouldRejectPdf() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "docs"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    // ==================== Null / empty / no-extension filenames ====================

    @Test
    @DisplayName("upload — rejects null original filename")
    void shouldRejectNullOriginalFilename() {
        MultipartFile file = new MockMultipartFile("file", (String) null, "image/jpeg", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "images"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects filename without extension")
    void shouldRejectNoExtension() {
        MultipartFile file = new MockMultipartFile("file", "photo", "image/jpeg", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "images"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("upload — rejects empty filename string")
    void shouldRejectEmptyFilename() {
        MultipartFile file = new MockMultipartFile("file", "", "image/jpeg", "content".getBytes());
        ServiceException ex = assertThrows(ServiceException.class, () -> ossService.upload(file, "images"));
        assertEquals(BusinessStatusEnum.FILE_TYPE_NOT_ALLOWED.getCode(), ex.getCode());
    }

    // ==================== Case insensitivity ====================

    @Test
    @DisplayName("upload — accepts .JPG (uppercase)")
    void shouldAcceptUppercaseJpg() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.JPG", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".JPG"), "result should end with .JPG");
    }

    @Test
    @DisplayName("upload — accepts .PNG (uppercase)")
    void shouldAcceptUppercasePng() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.PNG", "image/png", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".PNG"), "result should end with .PNG");
    }

    @Test
    @DisplayName("upload — accepts .GIF (uppercase)")
    void shouldAcceptUppercaseGif() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.GIF", "image/gif", "content".getBytes());
        String result = ossService.upload(file, "images");
        assertNotNull(result);
        assertTrue(result.endsWith(".GIF"), "result should end with .GIF");
    }

    // ==================== Directory prefix behavior ====================

    @Test
    @DisplayName("upload — with dir produces prefixed key")
    void shouldPrefixWithDir() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, "product/2026");
        assertNotNull(result);
        assertTrue(result.startsWith("product/2026/"), "result should start with the dir prefix");
    }

    @Test
    @DisplayName("upload — with null dir produces no prefix")
    void shouldHandleNullDir() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, null);
        assertNotNull(result);
        assertFalse(result.contains("/"), "result should not contain a slash when dir is null");
    }

    @Test
    @DisplayName("upload — with empty dir produces no prefix")
    void shouldHandleEmptyDir() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "content".getBytes());
        String result = ossService.upload(file, "");
        assertNotNull(result);
        assertFalse(result.contains("/"), "result should not contain a slash when dir is empty");
    }

    // ==================== Non-network InputStream upload ====================

    @Test
    @DisplayName("upload(InputStream, fileName) — returns the fileName unchanged")
    void shouldUploadInputStream() {
        InputStream is = new ByteArrayInputStream("fake-image-content".getBytes());
        String fileName = "test/abc.jpg";
        String result = ossService.upload(is, fileName);
        assertEquals(fileName, result, "should return the same fileName");
    }
}
