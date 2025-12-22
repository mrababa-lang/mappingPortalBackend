package com.slashdata.vehicleportal.dto;

import java.time.LocalDateTime;

public class AdpHistoryEntryDto {

    private final String id;
    private final String action;
    private final String details;
    private final LocalDateTime createdAt;
    private final String source;
    private final String userEmail;
    private final String mappingId;

    public AdpHistoryEntryDto(String id, String action, String details, LocalDateTime createdAt, String source,
                              String userEmail, String mappingId) {
        this.id = id;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
        this.source = source;
        this.userEmail = userEmail;
        this.mappingId = mappingId;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getSource() {
        return source;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMappingId() {
        return mappingId;
    }
}
