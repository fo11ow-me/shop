package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Oss API tests")
class OssApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setUp() {
        CustomUserDetails admin = new CustomUserDetails();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        admin.setEnabled(true);
        adminToken = jwtUtil.generateToken(admin, "/admin");
    }

    @Test
    @DisplayName("upload — 空文件返回错误")
    void shouldRejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/admin/oss/upload")
                        .file(emptyFile)
                        .param("dir", "product")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(901));
    }

    @Test
    @DisplayName("upload — 未认证请求被拒绝")
    void shouldRejectUnauthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "test".getBytes());

        mockMvc.perform(multipart("/admin/oss/upload")
                        .file(file)
                        .param("dir", "product"))
                .andExpect(status().is(401));
    }

    @Test
    @DisplayName("download — 未认证请求被拒绝")
    void shouldRejectUnauthenticatedDownload() throws Exception {
        mockMvc.perform(multipart("/admin/oss/download")
                        .param("key", "product/test.png"))
                .andExpect(status().is(401));
    }
}
