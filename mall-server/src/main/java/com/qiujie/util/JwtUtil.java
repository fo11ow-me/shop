package com.qiujie.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import com.qiujie.entity.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.admin-secret}")
    private String adminSecret;

    @Value("${jwt.portal-secret}")
    private String portalSecret;

    @Value("${jwt.expiration:7200000}")
    private long jwtExpiration;

    private SecretKey getSignInKey(String secret) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String selectSecret(String requestUri) {
        if (requestUri != null && requestUri.startsWith("/admin")) return adminSecret;
        if (requestUri != null && requestUri.startsWith("/portal")) return portalSecret;
        return null;
    }

    private SecretKey getKeyByUri(String requestUri) {
        String secret = selectSecret(requestUri);
        if (secret == null) return null;
        return getSignInKey(secret);
    }

    private SecretKey getKeyByPrefix(String pathPrefix) {
        if ("/admin".equals(pathPrefix)) return getSignInKey(adminSecret);
        if ("/portal".equals(pathPrefix)) return getSignInKey(portalSecret);
        return null;
    }

    // ==================== Token 解析（路径感知） ====================

    private Claims extractAllClaims(String token, String requestUri) {
        SecretKey key = getKeyByUri(requestUri);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token, String requestUri) {
        return extractClaim(token, Claims::getSubject, requestUri);
    }

    public Integer extractUserId(String token, String requestUri) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class), requestUri);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String requestUri) {
        try {
            final Claims claims = extractAllClaims(token, requestUri);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            return null;
        }
    }

    public Date extractExpiration(String token, String requestUri) {
        return extractClaim(token, Claims::getExpiration, requestUri);
    }

    /**
     * 从令牌中提取 JWT ID（jti），用于黑名单标识
     */
    public String extractJti(String token, String requestUri) {
        return extractClaim(token, claims -> claims.get("jti", String.class), requestUri);
    }

    /**
     * 计算令牌剩余有效秒数
     */
    public long extractRemainingSeconds(String token, String requestUri) {
        Date expiration = extractExpiration(token, requestUri);
        if (expiration == null) return 0;
        long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0);
    }

    public boolean isTokenExpired(String token, String requestUri) {
        Date expiration = extractExpiration(token, requestUri);
        return expiration == null || expiration.before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails, String requestUri) {
        final String username = extractUsername(token, requestUri);
        if (username == null) return false;
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token, requestUri);
    }

    // ==================== Token 签发 ====================

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration, String pathPrefix) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKeyByPrefix(pathPrefix))
                .compact();
    }

    public String generateToken(UserDetails userDetails, String pathPrefix) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", ((CustomUserDetails) userDetails).getUserId());
        claims.put("jti", UUID.randomUUID().toString().replace("-", ""));
        return generateToken(claims, userDetails, pathPrefix);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String pathPrefix) {
        return buildToken(extraClaims, userDetails, jwtExpiration, pathPrefix);
    }
}
