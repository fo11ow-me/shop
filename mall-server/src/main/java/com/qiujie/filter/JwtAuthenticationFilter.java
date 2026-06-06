package com.qiujie.filter;

import com.qiujie.service.CustomUserDetailsService;
import com.qiujie.util.JwtUtil;
import com.qiujie.util.RedisUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public JwtAuthenticationFilter(CustomUserDetailsService customUserDetailsService,
                                   JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String authorization = request.getHeader("Authorization");

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7).trim();
            if (StringUtils.hasText(token)) {
                String username = null;
                try {
                    username = jwtUtil.extractUsername(token, requestUri);
                } catch (Exception e) {
                    this.logger.warn("JWT parse failed: " + e.getMessage());
                }
                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // JWT 黑名单检查
                    String jti = jwtUtil.extractJti(token, requestUri);
                    if (jti != null && redisUtil.hasKey("token:blacklist:" + jti)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    try {
                        UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
                        if (userDetails != null && jwtUtil.isTokenValid(token, userDetails, requestUri)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    } catch (Exception e) {
                        this.logger.warn("Failed to load user: " + e.getMessage());
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
