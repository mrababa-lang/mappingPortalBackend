package com.slashdata.vehicleportal.dto;

import jakarta.servlet.http.HttpServletRequest;

public record AuditRequestContext(String ipAddress, String userAgent) {

    public static AuditRequestContext from(HttpServletRequest request) {
        if (request == null) {
            return new AuditRequestContext(null, null);
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");
        String ip = null;
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            ip = forwardedFor.split(",")[0].trim();
        }
        if ((ip == null || ip.isBlank()) && realIp != null && !realIp.isBlank()) {
            ip = realIp.trim();
        }
        if ((ip == null || ip.isBlank()) && request.getRemoteAddr() != null) {
            ip = request.getRemoteAddr();
        }
        String userAgent = request.getHeader("User-Agent");
        return new AuditRequestContext(ip, userAgent);
    }
}
