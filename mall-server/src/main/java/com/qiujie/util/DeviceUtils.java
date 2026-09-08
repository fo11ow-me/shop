package com.qiujie.util;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 设备类型识别工具 —— 从 User-Agent 解析客户端设备类型
 *
 * @author qiujie
 */
public class DeviceUtils {

    private DeviceUtils() {}

    /**
     * 从请求中解析设备类型
     * @return "pc" / "mobile" / "pad" / "miniProgram"
     */
    public static String getRequestDevice(HttpServletRequest request) {
        if (request == null) return "pc";
        String ua = request.getHeader("User-Agent");
        if (ua == null) return "pc";

        if (isMiniProgram(ua)) return "miniProgram";
        if (isPad(ua)) return "pad";

        UserAgent parsed = UserAgentUtil.parse(ua);
        if (parsed.isMobile()) return "mobile";
        return "pc";
    }

    private static boolean isMiniProgram(String ua) {
        return ua.toLowerCase().contains("micromessenger")
                && ua.toLowerCase().contains("miniprogram");
    }

    private static boolean isPad(String ua) {
        String lower = ua.toLowerCase();
        if (lower.contains("ipad")) return true;
        return lower.contains("android") && !lower.contains("mobile");
    }
}
