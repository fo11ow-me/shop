package com.qiujie.interceptor;

import com.qiujie.annotation.RateLimit;
import com.qiujie.enums.BusinessStatusEnum;
import com.qiujie.exception.ServiceException;
import com.qiujie.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 接口限流拦截器
 * <p>
 * 对有 {@link RateLimit} 注解的方法进行 Redis 计数限流，
 * 使用 INCR + EXPIRE 在窗口期内限制同一 IP 的请求次数。
 * </p>
 *
 * @author qiujie
 * @date 2026/05/30
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;
    private final boolean rateLimitEnabled;

    public RateLimitInterceptor(RedisUtil redisUtil,
                                 @Value("${rate-limit.enabled:true}") boolean rateLimitEnabled) {
        this.redisUtil = redisUtil;
        this.rateLimitEnabled = rateLimitEnabled;
    }

    /**
     * 请求处理前进行限流检查
     *
     * @param request  当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler  本次请求的目标处理器
     * @return true 放行，false 拦截
     * @throws ServiceException 超过限流阈值时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        if (!rateLimitEnabled) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }
        String clientIp = getClientIp(request);
        String redisKey = rateLimit.key() + clientIp;
        long current = redisUtil.increment(redisKey, 1L);
        if (current == 1) {
            redisUtil.expire(redisKey, rateLimit.window());
        }
        if (current > rateLimit.limit()) {
            throw new ServiceException(BusinessStatusEnum.RATE_LIMIT_EXCEEDED.getCode(),
                    rateLimit.message());
        }
        return true;
    }

    /**
     * 获取客户端真实 IP
     * <p>
     * 优先取 X-Forwarded-For 和 X-Real-IP 请求头以支持反向代理场景，
     * 兜底使用 {@link HttpServletRequest#getRemoteAddr()}。
     * </p>
     *
     * @param request HTTP 请求
     * @return 客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }
}
