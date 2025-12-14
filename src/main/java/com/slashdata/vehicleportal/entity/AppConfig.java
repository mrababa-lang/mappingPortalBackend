package com.slashdata.vehicleportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enable_ai")
    private boolean enableAI = false;

    @Column(name = "api_key")
    private String apiKey = "";

    @Column(name = "ai_confidence_threshold")
    private Double aiConfidenceThreshold = 0.0;

    @Column(name = "maintenance_mode")
    private boolean maintenanceMode = false;

    @Column(name = "enable_audit_log")
    private boolean enableAuditLog = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isEnableAI() {
        return enableAI;
    }

    public void setEnableAI(boolean enableAI) {
        this.enableAI = enableAI;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Double getAiConfidenceThreshold() {
        return aiConfidenceThreshold;
    }

    public void setAiConfidenceThreshold(Double aiConfidenceThreshold) {
        this.aiConfidenceThreshold = aiConfidenceThreshold;
    }

    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public boolean isEnableAuditLog() {
        return enableAuditLog;
    }

    public void setEnableAuditLog(boolean enableAuditLog) {
        this.enableAuditLog = enableAuditLog;
    }
}
