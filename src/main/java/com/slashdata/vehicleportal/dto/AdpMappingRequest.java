package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import jakarta.validation.constraints.NotNull;

public class AdpMappingRequest {

    @NotNull
    private String adpMasterId;

    private Long makeId;
    private String modelId;

    @NotNull
    private MappingStatus status;

    public String getAdpMasterId() {
        return adpMasterId;
    }

    public void setAdpMasterId(String adpMasterId) {
        this.adpMasterId = adpMasterId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
        this.makeId = makeId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public void setStatus(MappingStatus status) {
        this.status = status;
    }
}
