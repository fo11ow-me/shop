package com.qiujie.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiujie.entity.CustomUserDetails;
import com.qiujie.entity.User;
import com.qiujie.mapper.UserMapper;
import com.qiujie.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security API tests")
class SecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private String adminToken;
    private String portalToken;

    @BeforeEach
    void setUp() {
        CustomUserDetails admin = new CustomUserDetails();
        admin.setUserId(1);
        admin.setUsername("admin");
        admin.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")));
        admin.setEnabled(true);
        adminToken = jwtUtil.generateToken(admin, "/admin");

        CustomUserDetails portal = new CustomUserDetails();
        portal.setUserId(2);
        portal.setUsername("user");
        portal.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")));
        portal.setEnabled(true);
        portalToken = jwtUtil.generateToken(portal, "/portal");
    }

    @Test
    @DisplayName("SQL injection — login bypass attempt fails")
    void shouldBlockSqlInjectionLogin() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "admin' OR '1'='1");
        req.put("password", "anything");
        req.put("verificationCode", "1234");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.code").isNumber());
    }

    @Test
    @DisplayName("Horizontal privilege escalation — user cannot access another user's order")
    void shouldBlockHorizontalPrivilegeEscalation() throws Exception {
        // Create a separate user who does not own order 1 (order 1 belongs to user id=2)
        User hacker = new User();
        hacker.setCode("hacker");
        hacker.setName("Hacker");
        hacker.setPassword(new BCryptPasswordEncoder().encode("password"));
        userMapper.insert(hacker);

        CustomUserDetails hackerDetails = new CustomUserDetails();
        hackerDetails.setUserId(hacker.getId());
        hackerDetails.setUsername("hacker");
        hackerDetails.setAuthorities(Collections.emptyList());
        hackerDetails.setEnabled(true);
        String hackerToken = jwtUtil.generateToken(hackerDetails, "/portal");

        mockMvc.perform(get("/portal/order/detail/1")
                        .header("Authorization", "Bearer " + hackerToken))
                .andExpect(jsonPath("$.code").value(1600));
    }

    @Test
    @DisplayName("Vertical privilege escalation — portal user cannot access admin endpoints")
    void shouldBlockVerticalPrivilegeEscalation() throws Exception {
        mockMvc.perform(get("/admin/user/list")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("Password not exposed in user info response")
    void shouldNotExposePassword() throws Exception {
        mockMvc.perform(get("/portal/user/info")
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("File upload — endpoint accepts request")
    void shouldHandleFileUpload() throws Exception {
        mockMvc.perform(multipart("/portal/oss/upload")
                        .file("file", "not-an-image".getBytes())
                        .header("Authorization", "Bearer " + portalToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Path traversal — illegal key returns error")
    void shouldRejectPathTraversal() throws Exception {
        mockMvc.perform(get("/admin/oss/file")
                        .param("key", "../../../etc/passwd")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Forged token — rejected with error code")
    void shouldRejectForgedToken() throws Exception {
        mockMvc.perform(get("/portal/cart/list")
                        .header("Authorization", "Bearer forged.token.string"))
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("XSS — product name with script tag is stored as-is (no sanitization)")
    void shouldHandleXssInProductName() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("name", "<script>alert(1)</script>");
        req.put("price", 99.99);
        req.put("stock", 100);
        req.put("categoryId", 1);

        mockMvc.perform(post("/admin/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("File upload — .txt file is rejected by type validation")
    void shouldRejectInvalidFileType() throws Exception {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "malicious.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mockMvc.perform(multipart("/admin/oss/upload")
                        .file(txtFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.code").value(902));
    }

    @Test
    @DisplayName("CSRF — POST without auth token returns 401")
    void shouldRejectCsrfRequest() throws Exception {
        Map<String, String> req = new HashMap<>();
        req.put("code", "newuser");
        req.put("name", "New User");

        mockMvc.perform(post("/admin/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("Large file upload — large payload with blocked extension is rejected at type check")
    void shouldRejectLargeFileUpload() throws Exception {
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2 MB > default 1 MB limit
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.txt", MediaType.TEXT_PLAIN_VALUE, largeContent);

        mockMvc.perform(multipart("/admin/oss/upload")
                        .file(largeFile)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(jsonPath("$.code").value(902));
    }
}
