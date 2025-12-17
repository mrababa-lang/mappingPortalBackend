package com.slashdata.vehicleportal.dto;

public class CreateAdpMakeMappingRequest {

    private String adpMakeId;
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
