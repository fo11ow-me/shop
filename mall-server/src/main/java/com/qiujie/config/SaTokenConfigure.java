package com.qiujie.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置 — 登录校验 + 角色注解鉴权
 *
 * @author qiujie
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/admin/**").check(r -> StpUtil.checkLogin());
                    SaRouter.match("/portal/**").check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/portal/auth/verificationCode",
                        "/portal/auth/register",
                        "/portal/auth/login",
                        "/portal/product/**",
                        "/portal/seckill/**",
                        "/portal/ai/**",
                        "/admin/auth/verificationCode",
                        "/admin/auth/login",
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
