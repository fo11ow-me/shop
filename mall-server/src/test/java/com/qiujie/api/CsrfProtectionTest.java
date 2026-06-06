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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CSRF protection design verification for this JWT-based REST API.
 *
 * <h2>Design rationale</h2>
 *
 * This API uses a <strong>stateless JWT</strong> authentication model. Every
 * state-changing request must include a {@code Authorization: Bearer <token>}
 * header.  Because browsers do not automatically attach custom headers across
 * origins, JWT-in-Header is inherently immune to CSRF — an attacker cannot
 * forge a cross-origin request that carries the victim's JWT.
 *
 * <pre>{@code
 * // SecurityConfig.csurf(AbstractHttpConfigurer::disable)  — intentional
 * // SessionCreationPolicy.STATELESS                       — no session = no CSRF token
 * }</pre>
 *
 * <h3>What these tests prove</h3>
 *
 * <ol>
 *   <li><b>CSRF is disabled</b> — A POST without any token reaches the
 *       authentication layer (returns 401) instead of being blocked by a
 *       CSRF filter (which would return 403).</li>
 *   <li><b>JWT provides CSRF immunity</b> — A POST with a valid
 *       {@code Authorization} header succeeds; the same POST without it
 *       fails with 401.  This confirms that JWT, not a CSRF token, is the
 *       protection mechanism for state-changing operations.</li>
 *   <li><b>No CSRF cookie</b> — GET responses carry no {@code Set-Cookie}
 *       header for a CSRF token, confirming purely stateless operation.</li>
 *   <li><b>CORS is configured</b> — OPTIONS preflight requests return the
 *       standard CORS headers, so browser-based frontends (mall-admin on
 *       :3002, mall-portal on :3001) can call this API cross-origin.</li>
 * </ol>
 *
 * @see com.qiujie.config.SecurityConfig
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CSRF protection design verification")
class CsrfProtectionTest {

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
    @DisplayName("CSRF disabled — POST without token returns 401 (not 403), proving CSRF filter is absent")
    void shouldProveCsrfIsDisabled() throws Exception {
        /*
         * If CSRF were enabled, Spring Security's CsrfFilter would intercept this
         * POST (a state-changing method) before it ever reached the authentication
         * layer and return 403 Forbidden.
         *
         * Because CSRF is explicitly disabled (.csrf(AbstractHttpConfigurer::disable)),
         * the request flows through to the authorization layer, which finds no
         * authenticated principal and invokes the AuthenticationEntryPointHandler —
         * returning 401 Unauthorized with error code 1200.
         *
         * 401 vs 403 is the proof that CSRF protection is absent and JWT authentication
         * is the sole gatekeeper.
         */
        Map<String, Object> req = new HashMap<>();
        req.put("name", "csrf-test");
        req.put("price", 10.0);
        req.put("stock", 1);
        req.put("categoryId", 1);

        mockMvc.perform(post("/admin/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())       // 401, not 403
                .andExpect(jsonPath("$.code").value(1200)); // BusinessStatusEnum.UNAUTHORIZED
    }

    @Test
    @DisplayName("JWT provides CSRF immunity — POST with valid token succeeds, without fails with 401")
    void shouldProveJwtProvidesCsrfImmunity() throws Exception {
        /*
         * Stateless endpoints are protected by JWT, not by CSRF tokens.
         *
         * - With a valid Bearer token in the Authorization header, Spring Security
         *   establishes an authenticated principal and the request reaches the controller.
         * - Without the header, the request is rejected at the authentication layer.
         *
         * This mirrors exactly how a CSRF token would work — but using a header that
         * browsers never attach automatically, making it immune to CSRF attacks.
         */
        Map<String, Object> req = new HashMap<>();
        req.put("name", "jwt-protected-product");
        req.put("price", 29.99);
        req.put("stock", 50);
        req.put("categoryId", 1);

        // State-changing POST with valid JWT token — reaches the controller
        mockMvc.perform(post("/admin/product")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.code").value(200));

        // State-changing POST without any token — blocked at the auth layer
        mockMvc.perform(post("/admin/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(1200));
    }

    @Test
    @DisplayName("No CSRF cookie — GET response has no Set-Cookie header")
    void shouldNotReturnCsrfCookie() throws Exception {
        /*
         * In a stateless JWT architecture there are no server-side sessions and
         * therefore no session-derived CSRF tokens.  No Set-Cookie header should
         * appear in any response — neither JSESSIONID nor XSRF-TOKEN.
         *
         * If a future change reintroduces session state or cookie-based CSRF
         * protection, this test will fail, forcing the team to reconsider the
         * stateless design decision.
         */
        mockMvc.perform(get("/admin/product/list"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    @DisplayName("CORS configured — OPTIONS preflight returns CORS headers")
    void shouldReturnCorsHeadersOnPreflight() throws Exception {
        /*
         * Browser-based frontends (mall-admin on :3002, mall-portal on :3001)
         * must make cross-origin requests to this API on port 8800.  Before a
         * state-changing request, the browser sends an OPTIONS preflight to
         * verify CORS permissions.
         *
         * The CORS configuration in SecurityConfig allows all origins, methods,
         * and headers (with a 5-hour preflight cache).  These headers tell the
         * browser that cross-origin requests are permitted.
         */
        mockMvc.perform(options("/admin/product/list")
                        .header("Origin", "http://localhost:3001")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"))
                .andExpect(header().exists("Access-Control-Max-Age"));
    }

}
