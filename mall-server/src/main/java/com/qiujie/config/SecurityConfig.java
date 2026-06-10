package com.qiujie.config;

import com.qiujie.filter.JwtAuthenticationFilter;
import com.qiujie.handler.AccessDeniedExceptionHandler;
import com.qiujie.handler.AuthenticationEntryPointHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    private final AuthenticationEntryPointHandler authenticationEntryPointHandler;
    private final AccessDeniedExceptionHandler accessDeniedExceptionHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(AuthenticationEntryPointHandler authenticationEntryPointHandler,
                          AccessDeniedExceptionHandler accessDeniedExceptionHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationEntryPointHandler = authenticationEntryPointHandler;
        this.accessDeniedExceptionHandler = accessDeniedExceptionHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        corsConfiguration.setAllowedOrigins(origins);
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.addExposedHeader("X-Verification-Uuid");
        corsConfiguration.setMaxAge(Duration.ofHours(5));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/portal/product/**").permitAll()
                        .requestMatchers("/portal/auth/logout").permitAll()
                        .requestMatchers("/portal/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/portal/seckill/sessions", "/portal/seckill/sessions/upcoming", "/portal/seckill/server-time").permitAll()
                        .requestMatchers("/admin/auth/logout").permitAll()
                        .requestMatchers("/admin/auth/**").permitAll()
                        .requestMatchers("/admin/swagger-ui/**").permitAll()
                        .requestMatchers("/admin/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/actuator/health/**").permitAll()
                        .requestMatchers("/portal/**").authenticated()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPointHandler)
                        .accessDeniedHandler(accessDeniedExceptionHandler)
                )
                .build();
    }
}
