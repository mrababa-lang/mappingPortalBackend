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

    public MasterVehicleExportRow(String makeId, String makeName, String makeNameAr, Long modelId, String modelName,
                                  String modelNameAr, Long typeId, String typeName) {
        this.makeId = makeId;
        this.makeName = makeName;
        this.makeNameAr = makeNameAr;
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelNameAr = modelNameAr;
        this.typeId = typeId;
        this.typeName = typeName;
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
}
