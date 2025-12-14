package com.slashdata.vehicleportal.config;

import com.slashdata.vehicleportal.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Map<String, String> EXPECTED_MEDIA_TYPES = Map.of(
        "/api/makes/bulk", "application/json, text/csv",
        "/api/models/bulk", "application/json, text/csv",
        "/api/adp/master/upload", "application/json, text/csv"
    );

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
                                                                          HttpServletRequest request) {
        String expected = EXPECTED_MEDIA_TYPES.getOrDefault(request.getRequestURI(), "application/json");
        String message = "Unsupported media type. Expected: " + expected;
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResponse.of(message));
    }
}
