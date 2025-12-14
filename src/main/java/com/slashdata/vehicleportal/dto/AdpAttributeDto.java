package com.slashdata.vehicleportal.dto;

public class AdpAttributeDto {

    private String id;
    private String enDescription;
    private String arDescription;

    public AdpAttributeDto() {
    }

    public AdpAttributeDto(String id, String enDescription, String arDescription) {
        this.id = id;
        this.enDescription = enDescription;
        this.arDescription = arDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnDescription() {
        return enDescription;
    }

    public void setEnDescription(String enDescription) {
        this.enDescription = enDescription;
    }

    public String getArDescription() {
        return arDescription;
    }

    public void setArDescription(String arDescription) {
        this.arDescription = arDescription;
    }
}
