package com.slashdata.vehicleportal.dto;

public class CreateAdpMakeMappingRequest {

    private String adpMakeId;
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
