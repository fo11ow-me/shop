package com.qiujie.handler;

import com.alibaba.fastjson2.JSON;
import com.qiujie.dto.Response;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.util.WebUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class AccessDeniedExceptionHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String str = JSON.toJSONString(Response.error(BusinessStatusEnum.FORBIDDEN));
        WebUtil.renderString(response, str);
    }
}
