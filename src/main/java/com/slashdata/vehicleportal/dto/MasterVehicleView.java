package com.slashdata.vehicleportal.dto;

public class MasterVehicleView {

    private final String modelId;
    private final String modelName;
    private final String modelNameAr;
    private final Long makeId;
    private final String makeName;
    private final String makeNameAr;
    private final Long typeId;
    private final String typeName;

    public MasterVehicleView(String modelId, String modelName, String modelNameAr, Long makeId, String makeName,
                             String makeNameAr, Long typeId, String typeName) {
        this.modelId = modelId;
        this.modelName = modelName;
        this.modelNameAr = modelNameAr;
        this.makeId = makeId;
        this.makeName = makeName;
        this.makeNameAr = makeNameAr;
        this.typeId = typeId;
        this.typeName = typeName;
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

    public Long getMakeId() {
        return makeId;
    }

    public String getMakeName() {
        return makeName;
    }

    public String getMakeNameAr() {
        return makeNameAr;
    }

    public Long getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }
}
