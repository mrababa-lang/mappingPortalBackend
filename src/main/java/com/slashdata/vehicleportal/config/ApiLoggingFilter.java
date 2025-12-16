package com.slashdata.vehicleportal.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger API_LOGGER = LoggerFactory.getLogger("API_CALLS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long startTime = System.currentTimeMillis();
        String requestUri = requestWrapper.getRequestURI();
        if (request.getQueryString() != null) {
            requestUri += "?" + request.getQueryString();
        }
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String requestBody = getContentString(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getContentString(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());
            API_LOGGER.info(
                "{} {} status={} duration={}ms requestBody={} responseBody={}",
                requestWrapper.getMethod(),
                requestUri,
                responseWrapper.getStatus(),
                duration,
                requestBody,
                responseBody
            );
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getContentString(byte[] content, String characterEncoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        Charset charset = characterEncoding != null ? Charset.forName(characterEncoding) : StandardCharsets.UTF_8;
        return new String(content, charset);
    }
}
