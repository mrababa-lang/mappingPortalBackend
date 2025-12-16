package com.slashdata.vehicleportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class AdpMakeMapRequest {

    @NotBlank
    @JsonAlias({"adp_make_id", "adpMakeID"})
    private String adpMakeId;

    @NotBlank
    @JsonAlias({"sd_make_id", "sdMakeID"})
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
