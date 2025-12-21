package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.entity.AppConfig;
import com.slashdata.vehicleportal.repository.AppConfigRepository;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

    private final AppConfigRepository appConfigRepository;
    private final AiApiKeyCipher aiApiKeyCipher;
    private final AtomicReference<AppConfig> cachedConfig = new AtomicReference<>();

    public AppConfigService(AppConfigRepository appConfigRepository, AiApiKeyCipher aiApiKeyCipher) {
        this.appConfigRepository = appConfigRepository;
        this.aiApiKeyCipher = aiApiKeyCipher;
    }

    public AppConfig getConfig() {
        AppConfig cached = cachedConfig.get();
        if (cached != null) {
            return cached;
        }
        AppConfig config = loadOrCreate();
        cachedConfig.compareAndSet(null, config);
        return config;
    }

    public AppConfig updateConfig(AppConfig request) {
        AppConfig config = getConfig();
        config.setEnableAI(request.isEnableAI());
        config.setApiKey(request.getApiKey());
        if (request.getAiProvider() != null) {
            config.setAiProvider(request.getAiProvider());
        }
        if (request.getSystemInstruction() != null) {
            config.setSystemInstruction(request.getSystemInstruction());
        }
        if (request.getAiApiKey() != null) {
            config.setAiApiKey(aiApiKeyCipher.encrypt(request.getAiApiKey()));
        }
        config.setAiConfidenceThreshold(request.getAiConfidenceThreshold());
        config.setMaintenanceMode(request.isMaintenanceMode());
        config.setEnableAuditLog(request.isEnableAuditLog());
        return saveConfig(config);
    }

    public AppConfig saveConfig(AppConfig config) {
        AppConfig saved = appConfigRepository.save(config);
        cachedConfig.set(saved);
        return saved;
    }

    public String getSystemInstruction() {
        AppConfig config = getConfig();
        return config != null ? config.getSystemInstruction() : null;
    }

    public String getDecryptedAiApiKey() {
        AppConfig config = getConfig();
        if (config == null) {
            return null;
        }
        return aiApiKeyCipher.decrypt(config.getAiApiKey());
    }

    private AppConfig loadOrCreate() {
        return appConfigRepository.findTopByOrderByIdAsc()
            .orElseGet(() -> appConfigRepository.save(new AppConfig()));
    }
}
