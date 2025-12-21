package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AiConnectionResult;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.AppConfig;
import com.slashdata.vehicleportal.service.AiProviderService;
import com.slashdata.vehicleportal.service.AppConfigService;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT_LOG");

    private final AppConfigService appConfigService;
    private final AiProviderService aiProviderService;

    public ConfigController(AppConfigService appConfigService, AiProviderService aiProviderService) {
        this.appConfigService = appConfigService;
        this.aiProviderService = aiProviderService;
    }

    @GetMapping
    public ApiResponse<AppConfig> getConfig() {
        return ApiResponse.of(appConfigService.getConfig());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppConfig>> update(@RequestBody AppConfig request, Principal principal) {
        AppConfig existing = appConfigService.getConfig();
        String previousInstruction = existing.getSystemInstruction();
        AppConfig saved = appConfigService.updateConfig(request);
        String updatedInstruction = saved.getSystemInstruction();
        if (!Objects.equals(previousInstruction, updatedInstruction)) {
            String actor = principal != null ? principal.getName() : "unknown";
            AUDIT_LOGGER.info(
                "system_instruction updated by {} (previousLength={}, newLength={})",
                actor,
                previousInstruction != null ? previousInstruction.length() : 0,
                updatedInstruction != null ? updatedInstruction.length() : 0
            );
        }
        return ResponseEntity.ok(ApiResponse.of(saved));
    }

    @GetMapping("/test-ai-connection")
    public ResponseEntity<Map<String, Object>> testAiConnection() {
        try {
            AiConnectionResult result = aiProviderService.testConnection();
            return ResponseEntity.ok(Map.of(
                "status", result.getStatus(),
                "latency", result.getLatency()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", "error",
                "message", ex.getMessage()
            ));
        }
    }
}
