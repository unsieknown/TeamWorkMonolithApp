package com.mordiniaa.backend.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class IpAddrUtils {

    public String extractClientId(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null) {
            return forwarded.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
