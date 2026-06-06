package com.qiujie.unit;

import com.qiujie.entity.CustomUserDetails;
import com.qiujie.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil unit tests")
class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();

    private static final String ADMIN_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final String PORTAL_SECRET = "6A586E3272357538782F413F4428472B4B6250645367566B5970404E63526655";

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "adminSecret", ADMIN_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "portalSecret", PORTAL_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 7200000L);

        userDetails = new CustomUserDetails();
        userDetails.setUserId(1);
        userDetails.setUsername("admin");
        userDetails.setPassword("encoded_password");
        userDetails.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_admin")));
        userDetails.setAccountNonExpired(true);
        userDetails.setAccountNonLocked(true);
        userDetails.setCredentialsNonExpired(true);
        userDetails.setEnabled(true);
    }

    @Test
    @DisplayName("generate token — admin prefix includes userId in payload")
    void shouldGenerateTokenWithUserId() {
        String token = jwtUtil.generateToken(userDetails, "/admin");

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Integer userId = jwtUtil.extractUserId(token, "/admin/system/user/list");
        assertEquals(1, userId);
    }

    @Test
    @DisplayName("generate token — portal prefix works independently")
    void shouldGeneratePortalToken() {
        String token = jwtUtil.generateToken(userDetails, "/portal");

        assertNotNull(token);
        Integer userId = jwtUtil.extractUserId(token, "/portal/product/home");
        assertEquals(1, userId);
    }

    @Test
    @DisplayName("parse token — extract correct username")
    void shouldExtractUsername() {
        String token = jwtUtil.generateToken(userDetails, "/admin");
        String username = jwtUtil.extractUsername(token, "/admin/system/user/list");
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("parse token — extract expiration date")
    void shouldExtractExpiration() {
        String token = jwtUtil.generateToken(userDetails, "/admin");
        Date expiration = jwtUtil.extractExpiration(token, "/admin/system/user/list");

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("expired token — should return null username")
    void shouldDetectExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);
        String token = jwtUtil.generateToken(userDetails, "/admin");

        Thread.sleep(10);
        assertNull(jwtUtil.extractUsername(token, "/admin/system/user/list"));
    }

    @Test
    @DisplayName("forged token — random string should throw exception")
    void shouldRejectForgedToken() {
        assertThrows(JwtException.class, () -> {
            jwtUtil.extractUsername("this.is.not.a.valid.jwt.token", "/admin/system/user/list");
        });
    }

    @Test
    @DisplayName("cross-key isolation — admin secret can't verify portal token")
    void shouldRejectCrossKeyToken() {
        String portalToken = jwtUtil.generateToken(userDetails, "/portal");
        assertThrows(SignatureException.class, () -> {
            jwtUtil.extractUsername(portalToken, "/admin/system/user/list");
        });
    }

    @Test
    @DisplayName("cross-key isolation — portal secret can't verify admin token")
    void shouldRejectAdminTokenOnPortal() {
        String adminToken = jwtUtil.generateToken(userDetails, "/admin");
        assertThrows(SignatureException.class, () -> {
            jwtUtil.extractUsername(adminToken, "/portal/product/home");
        });
    }

    @Test
    @DisplayName("isTokenExpired — returns false for fresh token")
    void shouldNotBeExpired() {
        String token = jwtUtil.generateToken(userDetails, "/admin");
        assertFalse(jwtUtil.isTokenExpired(token, "/admin/system/user/list"));
    }

    @Test
    @DisplayName("isTokenValid — returns true for valid user and fresh token")
    void shouldBeValid() {
        String token = jwtUtil.generateToken(userDetails, "/admin");
        assertTrue(jwtUtil.isTokenValid(token, userDetails, "/admin/system/user/list"));
    }

    @Test
    @DisplayName("isTokenValid — returns false for mismatched username")
    void shouldBeInvalidForDifferentUser() {
        String token = jwtUtil.generateToken(userDetails, "/admin");
        CustomUserDetails other = new CustomUserDetails();
        other.setUsername("other_user");
        assertFalse(jwtUtil.isTokenValid(token, other, "/admin/system/user/list"));
    }

    @Test
    @DisplayName("isTokenValid — returns false for expired token (BUG-015)")
    void shouldReturnFalseForExpiredTokenInIsTokenValid() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);
        String token = jwtUtil.generateToken(userDetails, "/admin");
        Thread.sleep(10);
        assertFalse(jwtUtil.isTokenValid(token, userDetails, "/admin/system/user/list"));
    }

    @Test
    @DisplayName("isTokenExpired — returns true for expired token (BUG-015)")
    void shouldReturnTrueForExpiredTokenInIsTokenExpired() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 1L);
        String token = jwtUtil.generateToken(userDetails, "/admin");
        Thread.sleep(10);
        assertTrue(jwtUtil.isTokenExpired(token, "/admin/system/user/list"));
    }
}
