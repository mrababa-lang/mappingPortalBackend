package com.slashdata.vehicleportal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdpMakeMapRequest {

    @NotBlank
    @JsonAlias({"adp_make_id", "adpMakeID"})
    private String adpMakeId;

    @NotNull
    @JsonAlias({"sd_make_id", "sdMakeID"})
    private Long sdMakeId;

    public String getAdpMakeId() {
        return adpMakeId;
    }

    public void setAdpMakeId(String adpMakeId) {
        this.adpMakeId = adpMakeId;
    }

    public Long getSdMakeId() {
        return sdMakeId;
    }

    public void setSdMakeId(Long sdMakeId) {
        this.sdMakeId = sdMakeId;
    }
}
