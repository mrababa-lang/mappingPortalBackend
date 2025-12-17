package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import jakarta.validation.constraints.NotNull;

public class AdpMappingRequest {

    private String makeId;
    private Long modelId;

    @NotNull
    private MappingStatus status;

    public String getMakeId() {
        return makeId;
    }

    public void setMakeId(String makeId) {
        this.makeId = makeId;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public void setStatus(MappingStatus status) {
        this.status = status;
    }
}
