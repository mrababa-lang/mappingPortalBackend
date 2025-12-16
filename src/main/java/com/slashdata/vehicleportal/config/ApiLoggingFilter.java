package com.slashdata.vehicleportal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger API_LOGGER = LoggerFactory.getLogger("API_CALLS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestUri = request.getRequestURI();
        if (request.getQueryString() != null) {
            requestUri += "?" + request.getQueryString();
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            API_LOGGER.info("{} {} status={} duration={}ms", request.getMethod(), requestUri, response.getStatus(), duration);
        }
    }
}
