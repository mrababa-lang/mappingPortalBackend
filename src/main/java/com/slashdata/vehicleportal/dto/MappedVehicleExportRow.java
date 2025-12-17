package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;

public class MappedVehicleExportRow {

    private final String adpId;
    private final String adpMakeDescription;
    private final String adpModelDescription;
    private final String adpTypeDescription;
    private final String sdMakeName;
    private final String sdModelName;
    private final String sdTypeName;
    private final MappingStatus status;
    private final String mappedBy;
    private final LocalDateTime mappedAt;

    public MappedVehicleExportRow(String adpId, String adpMakeDescription, String adpModelDescription,
                                  String adpTypeDescription, String sdMakeName, String sdModelName,
                                  String sdTypeName, MappingStatus status, String mappedBy,
                                  LocalDateTime mappedAt) {
        this.adpId = adpId;
        this.adpMakeDescription = adpMakeDescription;
        this.adpModelDescription = adpModelDescription;
        this.adpTypeDescription = adpTypeDescription;
        this.sdMakeName = sdMakeName;
        this.sdModelName = sdModelName;
        this.sdTypeName = sdTypeName;
        this.status = status;
        this.mappedBy = mappedBy;
        this.mappedAt = mappedAt;
    }

    public String getAdpId() {
        return adpId;
    }

    public String getAdpMakeDescription() {
        return adpMakeDescription;
    }

    public String getAdpModelDescription() {
        return adpModelDescription;
    }

    public String getAdpTypeDescription() {
        return adpTypeDescription;
    }

    public String getSdMakeName() {
        return sdMakeName;
    }

    public String getSdModelName() {
        return sdModelName;
    }

    public String getSdTypeName() {
        return sdTypeName;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public LocalDateTime getMappedAt() {
        return mappedAt;
    }
}
