package com.qiujie.util;

import com.qiujie.entity.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static Integer getCurrentUserId() {
        CustomUserDetails details = (CustomUserDetails)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return details.getUserId();
    }
}
