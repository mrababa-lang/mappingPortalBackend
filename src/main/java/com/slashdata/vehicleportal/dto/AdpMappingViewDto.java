package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;

public class AdpMappingViewDto {

    private String adpId;
    private String makeEnDesc;
    private String modelEnDesc;
    private String typeEnDesc;
    private String status;
    private Long sdMakeId;
    private String sdMakeName;
    private String sdModelId;
    private String sdModelName;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;

    public AdpMappingViewDto(String adpId, String makeEnDesc, String modelEnDesc, String typeEnDesc,
                             MappingStatus status, Long sdMakeId, String sdMakeName, String sdModelId,
                             String sdModelName, Long updatedBy, LocalDateTime updatedAt,
                             LocalDateTime reviewedAt) {
        this.adpId = adpId;
        this.makeEnDesc = makeEnDesc;
        this.modelEnDesc = modelEnDesc;
        this.typeEnDesc = typeEnDesc;
        this.status = status != null ? status.name() : "UNMAPPED";
        this.sdMakeId = sdMakeId;
        this.sdMakeName = sdMakeName;
        this.sdModelId = sdModelId;
        this.sdModelName = sdModelName;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.reviewedAt = reviewedAt;
    }

    public String getAdpId() {
        return adpId;
    }

    public void setAdpId(String adpId) {
        this.adpId = adpId;
    }

    public String getMakeEnDesc() {
        return makeEnDesc;
    }

    public void setMakeEnDesc(String makeEnDesc) {
        this.makeEnDesc = makeEnDesc;
    }

    public String getModelEnDesc() {
        return modelEnDesc;
    }

    public void setModelEnDesc(String modelEnDesc) {
        this.modelEnDesc = modelEnDesc;
    }

    public String getTypeEnDesc() {
        return typeEnDesc;
    }

    public void setTypeEnDesc(String typeEnDesc) {
        this.typeEnDesc = typeEnDesc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSdMakeId() {
        return sdMakeId;
    }

    public void setSdMakeId(Long sdMakeId) {
        this.sdMakeId = sdMakeId;
    }

    public String getSdMakeName() {
        return sdMakeName;
    }

    public void setSdMakeName(String sdMakeName) {
        this.sdMakeName = sdMakeName;
    }

    public String getSdModelId() {
        return sdModelId;
    }

    public void setSdModelId(String sdModelId) {
        this.sdModelId = sdModelId;
    }

    public String getSdModelName() {
        return sdModelName;
    }

    public void setSdModelName(String sdModelName) {
        this.sdModelName = sdModelName;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
