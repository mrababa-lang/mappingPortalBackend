package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import jakarta.validation.constraints.NotNull;

public class AdpMappingRequest {

    private String makeId;
    private Long modelId;
    private Integer confidence;
    private String matchingEngine;
    private Boolean autoPropagated;

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

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getMatchingEngine() {
        return matchingEngine;
    }

    public void setMatchingEngine(String matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    public Boolean getAutoPropagated() {
        return autoPropagated;
    }

    public void setAutoPropagated(Boolean autoPropagated) {
        this.autoPropagated = autoPropagated;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public void setStatus(MappingStatus status) {
        this.status = status;
    }
}
