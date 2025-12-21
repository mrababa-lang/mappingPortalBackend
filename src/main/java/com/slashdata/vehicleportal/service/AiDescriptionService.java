package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.entity.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class AiDescriptionService {

    private final AppConfigService appConfigService;
    private final AiProviderService aiProviderService;

    public AiDescriptionService(AppConfigService appConfigService, AiProviderService aiProviderService) {
        this.appConfigService = appConfigService;
        this.aiProviderService = aiProviderService;
    }

    public String generateDescription(String name) {
        String sanitized = name != null ? name.trim() : "";
        if (sanitized.isBlank()) {
            return "";
        }
        AppConfig config = appConfigService.getConfig();
        if (config == null || !config.isEnableAI()) {
            return sanitized;
        }
        String prompt = "Write a concise vehicle description for: " + sanitized;
        return aiProviderService.generateContent(prompt);
    }
}
