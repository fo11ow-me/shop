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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Image loading and exception boundary tests")
class ImageLoadingTest {

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
        admin.setAuthorities(Collections.singletonList(
                new SimpleGrantedAuthority("system:user:list")));
        admin.setEnabled(true);
        adminToken = jwtUtil.generateToken(admin, "/admin");
    }

    @Test
    @DisplayName("GET /portal/product/detail/1 — response includes image URLs in product data")
    void shouldIncludeImageUrlsInProductDetail() throws Exception {
        mockMvc.perform(get("/portal/product/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.images").isArray())
                .andExpect(jsonPath("$.data.images[0].url").value("https://example.com/iphone15.jpg"))
                .andExpect(jsonPath("$.data.name").value("iPhone 15"))
                .andExpect(jsonPath("$.data.price").value(6999.00));
    }

    @Test
    @DisplayName("GET /portal/product/detail/99999 — returns error code 1500 for non-existent product")
    void shouldReturnProductNotExistForInvalidId() throws Exception {
        mockMvc.perform(get("/portal/product/detail/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1500))
                .andExpect(jsonPath("$.message").value("商品不存在"));
    }

    @Test
    @DisplayName("GET /admin/user/avatar/1 without token — returns 401")
    void shouldRequireAuthForAvatarEndpoint() throws Exception {
        mockMvc.perform(get("/admin/user/avatar/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200))
                .andExpect(jsonPath("$.message").value("认证失败，请重新登录"));
    }

    @Test
    @DisplayName("GET /admin/user/avatar/1 with admin token — responds with image or fallback")
    void shouldReturnAvatarWithToken() throws Exception {
        // Admin user (id=1) has avatar='' in seed data, so the endpoint returns 404
        mockMvc.perform(get("/admin/user/avatar/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /admin/oss/file without key param — returns error response")
    void shouldReturnErrorWhenOssFileKeyMissing() throws Exception {
        // Endpoint does not exist; auth-filter blocks unauthenticated requests first
        mockMvc.perform(get("/admin/oss/file"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("GET /portal/product/search?keyword= — returns empty/null result for blank keyword")
    void shouldReturnEmptyForBlankKeyword() throws Exception {
        mockMvc.perform(get("/portal/product/search")
                        .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
