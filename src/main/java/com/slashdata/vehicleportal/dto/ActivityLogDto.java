package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.ADPMapping;
import java.time.LocalDateTime;

public class ActivityLogDto {
    private String id;
    private String adpMasterId;
    private String status;
    private LocalDateTime updatedAt;

    public static ActivityLogDto from(ADPMapping mapping) {
        ActivityLogDto dto = new ActivityLogDto();
        dto.setId(mapping.getId());
        dto.setAdpMasterId(mapping.getAdpMaster().getId());
        dto.setStatus(mapping.getStatus().name());
        dto.setUpdatedAt(mapping.getUpdatedAt());
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdpMasterId() {
        return adpMasterId;
    }

    public void setAdpMasterId(String adpMasterId) {
        this.adpMasterId = adpMasterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
