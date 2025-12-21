package com.slashdata.vehicleportal.dto;

public class MasterVehicleExportRow {

    private final String makeId;
    private final String makeName;
    private final String makeNameAr;
    private final Long modelId;
    private final String modelName;
    private final String modelNameAr;
    private final Long typeId;
    private final String typeName;
    private final String kindCode;
    private final String kindEnDesc;
    private final String kindArDesc;

    public MasterVehicleExportRow(String makeId, String makeName, String makeNameAr, Long modelId, String modelName,
                                  String modelNameAr, Long typeId, String typeName, String kindCode, String kindEnDesc,
                                  String kindArDesc) {
        this.makeId = makeId;
        this.makeName = makeName;
        this.makeNameAr = makeNameAr;
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelNameAr = modelNameAr;
        this.typeId = typeId;
        this.typeName = typeName;
        this.kindCode = kindCode;
        this.kindEnDesc = kindEnDesc;
        this.kindArDesc = kindArDesc;
    }

    public String getMakeId() {
        return makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public String getMakeNameAr() {
        return makeNameAr;
    }

    public Long getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelNameAr() {
        return modelNameAr;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getKindCode() {
        return kindCode;
    }

    public String getKindEnDesc() {
        return kindEnDesc;
    }

    public String getKindArDesc() {
        return kindArDesc;
    }
}
