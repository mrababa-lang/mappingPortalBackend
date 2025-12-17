package com.slashdata.vehicleportal.dto;

public class MasterVehicleExportRow {

    private final Long makeId;
    private final String makeName;
    private final String makeNameAr;
    private final String modelId;
    private final String modelName;
    private final String modelNameAr;
    private final String typeId;
    private final String typeName;

    public MasterVehicleExportRow(Long makeId, String makeName, String makeNameAr, String modelId, String modelName,
                                  String modelNameAr, String typeId, String typeName) {
        this.makeId = makeId;
        this.makeName = makeName;
        this.makeNameAr = makeNameAr;
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelNameAr = modelNameAr;
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public Long getMakeId() {
        return makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public String getMakeNameAr() {
        return makeNameAr;
    }

    public String getModelId() {
        return modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public String getModelNameAr() {
        return modelNameAr;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }
}
