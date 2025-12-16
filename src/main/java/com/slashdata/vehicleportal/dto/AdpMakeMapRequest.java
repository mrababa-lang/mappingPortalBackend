package com.slashdata.vehicleportal.dto;

import jakarta.validation.constraints.NotBlank;

public class AdpMakeMapRequest {

    @NotBlank
    private String adpMakeId;

    @NotBlank
    private String sdMakeId;

    public String getAdpMakeId() {
        return adpMakeId;
    }

    public void setAdpMakeId(String adpMakeId) {
        this.adpMakeId = adpMakeId;
    }

    public String getSdMakeId() {
        return sdMakeId;
    }

    public void setSdMakeId(String sdMakeId) {
        this.sdMakeId = sdMakeId;
    }
}
