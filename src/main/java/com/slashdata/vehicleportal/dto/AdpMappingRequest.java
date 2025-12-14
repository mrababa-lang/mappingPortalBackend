package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import jakarta.validation.constraints.NotNull;

public class AdpMappingRequest {

    @NotNull
    private Long adpMasterId;

    private Long makeId;
    private Long modelId;

    @NotNull
    private MappingStatus status;

    public Long getAdpMasterId() {
        return adpMasterId;
    }

    public void setAdpMasterId(Long adpMasterId) {
        this.adpMasterId = adpMasterId;
    }

    public Long getMakeId() {
        return makeId;
    }

    public void setMakeId(Long makeId) {
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
