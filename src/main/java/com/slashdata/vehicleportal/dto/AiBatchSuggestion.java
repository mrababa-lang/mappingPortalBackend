package com.slashdata.vehicleportal.dto;

public class AiBatchSuggestion {

    private String adpId;
    private String sdMakeId;
    private Long sdModelId;
    private Integer score;

    public AiBatchSuggestion(String adpId, String sdMakeId, Long sdModelId, Integer score) {
        this.adpId = adpId;
        this.sdMakeId = sdMakeId;
        this.sdModelId = sdModelId;
        this.score = score;
    }

    public AiBatchSuggestion() {
    }

    public String getAdpId() {
        return adpId;
    }

    public void setAdpId(String adpId) {
        this.adpId = adpId;
    }

    public String getSdMakeId() {
        return sdMakeId;
    }

    public void setSdMakeId(String sdMakeId) {
        this.sdMakeId = sdMakeId;
    }

    public Long getSdModelId() {
        return sdModelId;
    }

    public void setSdModelId(Long sdModelId) {
        this.sdModelId = sdModelId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
