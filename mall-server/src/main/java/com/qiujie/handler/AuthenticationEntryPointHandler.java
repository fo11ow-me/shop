package com.qiujie.handler;

import com.alibaba.fastjson2.JSON;
import com.qiujie.dto.Response;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.util.WebUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class AuthenticationEntryPointHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String str = JSON.toJSONString(Response.error(BusinessStatusEnum.UNAUTHORIZED));
        WebUtil.renderString(response, str);
    }
}
