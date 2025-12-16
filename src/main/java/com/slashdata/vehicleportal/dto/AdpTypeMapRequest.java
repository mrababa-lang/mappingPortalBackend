package com.slashdata.vehicleportal.dto;

import jakarta.validation.constraints.NotBlank;

public class AdpTypeMapRequest {

    @NotBlank
    private String adpTypeId;

    @NotBlank
    private String sdTypeId;

    public String getAdpTypeId() {
        return adpTypeId;
    }

    public void setAdpTypeId(String adpTypeId) {
        this.adpTypeId = adpTypeId;
    }

    public String getSdTypeId() {
        return sdTypeId;
    }

    public void setSdTypeId(String sdTypeId) {
        this.sdTypeId = sdTypeId;
    }
}
