package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.ADPMapping;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class RecentActivityDto {
    private String id;
    private String user;
    private String action;
    private String details;
    private OffsetDateTime timestamp;

    public static RecentActivityDto from(ADPMapping mapping) {
        RecentActivityDto dto = new RecentActivityDto();
        dto.setId(mapping.getId());
        dto.setUser(mapping.getUpdatedBy() != null ? mapping.getUpdatedBy().getFullName() : "System");
        dto.setAction(mapping.getStatus() != null ? mapping.getStatus().name() : "UPDATED");
        String makeName = mapping.getAdpMaster() != null ? mapping.getAdpMaster().getMakeEnDesc() : "";
        String modelName = mapping.getAdpMaster() != null ? mapping.getAdpMaster().getModelEnDesc() : "";
        String mappedMake = mapping.getMake() != null ? mapping.getMake().getName() : null;
        String mappedModel = mapping.getModel() != null ? mapping.getModel().getName() : null;
        StringBuilder detailsBuilder = new StringBuilder();
        if (!makeName.isBlank() || !modelName.isBlank()) {
            detailsBuilder.append("Mapped ").append(makeName).append(" ").append(modelName).toString();
        }
        if (mappedMake != null || mappedModel != null) {
            if (detailsBuilder.length() > 0) {
                detailsBuilder.append(" to ");
            }
            detailsBuilder.append(mappedMake != null ? mappedMake : "");
            if (mappedModel != null) {
                detailsBuilder.append(" ").append(mappedModel);
            }
        }
        dto.setDetails(detailsBuilder.length() > 0 ? detailsBuilder.toString().trim() : "Mapping updated");
        dto.setTimestamp(mapping.getUpdatedAt() != null ? mapping.getUpdatedAt().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC));
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

