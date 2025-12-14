package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @PostMapping("/suggest-mapping")
    public ApiResponse<Map<String, Object>> suggestMapping(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("match", payload.getOrDefault("description", ""));
        response.put("confidence", 0.0);
        return ApiResponse.of(response);
    }

    @PostMapping("/suggest-models")
    public ApiResponse<Map<String, Object>> suggestModels(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("makeName", payload.get("makeName"));
        response.put("suggestions", new String[]{});
        return ApiResponse.of(response);
    }

    @PostMapping("/generate-description")
    public ApiResponse<Map<String, Object>> generateDescription(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("description", payload.getOrDefault("name", ""));
        return ApiResponse.of(response);
    }
}
