package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.AppConfig;
import com.slashdata.vehicleportal.repository.AppConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private final AppConfigRepository appConfigRepository;

    public ConfigController(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @GetMapping
    public ApiResponse<AppConfig> getConfig() {
        return ApiResponse.of(loadOrCreate());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppConfig>> update(@RequestBody AppConfig request) {
        AppConfig config = loadOrCreate();
        config.setEnableAI(request.isEnableAI());
        config.setApiKey(request.getApiKey());
        config.setAiConfidenceThreshold(request.getAiConfidenceThreshold());
        config.setMaintenanceMode(request.isMaintenanceMode());
        config.setEnableAuditLog(request.isEnableAuditLog());
        return ResponseEntity.ok(ApiResponse.of(appConfigRepository.save(config)));
    }

    private AppConfig loadOrCreate() {
        return appConfigRepository.findTopByOrderByIdAsc()
            .orElseGet(() -> appConfigRepository.save(new AppConfig()));
    }
}
