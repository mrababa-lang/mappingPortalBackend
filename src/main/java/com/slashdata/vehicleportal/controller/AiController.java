package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AiBatchMatchRequest;
import com.slashdata.vehicleportal.dto.AiBatchMatchResult;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.service.AdpMappingService;
import com.slashdata.vehicleportal.service.AiBatchMatchingService;
import com.slashdata.vehicleportal.service.AiDescriptionService;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiBatchMatchingService aiBatchMatchingService;
    private final AdpMappingService adpMappingService;
    private final AiDescriptionService aiDescriptionService;

    public AiController(AiBatchMatchingService aiBatchMatchingService, AdpMappingService adpMappingService,
                        AiDescriptionService aiDescriptionService) {
        this.aiBatchMatchingService = aiBatchMatchingService;
        this.adpMappingService = adpMappingService;
        this.aiDescriptionService = aiDescriptionService;
    }

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
        String name = String.valueOf(payload.getOrDefault("name", ""));
        String description = aiDescriptionService.generateDescription(name);
        Map<String, Object> response = new HashMap<>();
        response.put("description", description);
        return ApiResponse.of(response);
    }

    @PostMapping("/batch-match")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN', 'MAPPING_USER')")
    public ApiResponse<AiBatchMatchResult> batchMatch(@RequestBody AiBatchMatchRequest request,
                                                      Principal principal) {
        User actor = adpMappingService.findUser(principal != null ? principal.getName() : null);
        return ApiResponse.of(aiBatchMatchingService.processBatchMatching(actor, request));
    }
}
