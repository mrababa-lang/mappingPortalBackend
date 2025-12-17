package com.slashdata.vehicleportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdpTypeMapRequest {

    @NotBlank
    @JsonAlias({"adp_type_id", "adpTypeID"})
    private String adpTypeId;

    @NotNull
    @JsonAlias({"sd_type_id", "sdTypeID"})
    private Long sdTypeId;

    public String getAdpTypeId() {
        return adpTypeId;
    }

    public void setAdpTypeId(String adpTypeId) {
        this.adpTypeId = adpTypeId;
    }

    public Long getSdTypeId() {
        return sdTypeId;
    }

    public void setSdTypeId(Long sdTypeId) {
        this.sdTypeId = sdTypeId;
    }
}
