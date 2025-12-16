package com.slashdata.vehicleportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class AdpTypeMapRequest {

    @NotBlank
    @JsonAlias({"adp_type_id", "adpTypeID"})
    private String adpTypeId;

    @NotBlank
    @JsonAlias({"sd_type_id", "sdTypeID"})
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
