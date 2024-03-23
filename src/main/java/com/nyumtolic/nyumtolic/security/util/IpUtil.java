package com.nyumtolic.nyumtolic.security.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {
    public static String getClientIp(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null) {
            clientIP = request.getHeader("Proxy-Client-IP");
        }
        if (clientIP == null) {
            clientIP = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIP == null) {
            clientIP = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIP == null) {
            clientIP = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIP == null) {
            clientIP = request.getRemoteAddr();
        }
        return clientIP;
    }
}
