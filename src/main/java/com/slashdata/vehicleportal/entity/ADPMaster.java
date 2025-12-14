package com.slashdata.vehicleportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "adp_master")
public class ADPMaster {

    @Id
    @Column(length = 36, columnDefinition = "char(36)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "adp_make_id", length = 50)
    private String adpMakeId;

    @Column(name = "make_en_desc", length = 200)
    private String makeEnDesc;

    @Column(name = "make_ar_desc", length = 200)
    private String makeArDesc;

    @Column(name = "adp_model_id", length = 50)
    private String adpModelId;

    @Column(name = "model_en_desc", length = 200)
    private String modelEnDesc;

    @Column(name = "model_ar_desc", length = 200)
    private String modelArDesc;

    @Column(name = "adp_type_id", length = 50)
    private String adpTypeId;

    @Column(name = "type_en_desc", length = 200)
    private String typeEnDesc;

    @Column(name = "type_ar_desc", length = 200)
    private String typeArDesc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdpMakeId() {
        return adpMakeId;
    }

    public void setAdpMakeId(String adpMakeId) {
        this.adpMakeId = adpMakeId;
    }

    public String getMakeEnDesc() {
        return makeEnDesc;
    }

    public void setMakeEnDesc(String makeEnDesc) {
        this.makeEnDesc = makeEnDesc;
    }

    public String getMakeArDesc() {
        return makeArDesc;
    }

    public void setMakeArDesc(String makeArDesc) {
        this.makeArDesc = makeArDesc;
    }

    public String getAdpModelId() {
        return adpModelId;
    }

    public void setAdpModelId(String adpModelId) {
        this.adpModelId = adpModelId;
    }

    public String getModelEnDesc() {
        return modelEnDesc;
    }

    public void setModelEnDesc(String modelEnDesc) {
        this.modelEnDesc = modelEnDesc;
    }

    public String getModelArDesc() {
        return modelArDesc;
    }

    public void setModelArDesc(String modelArDesc) {
        this.modelArDesc = modelArDesc;
    }

    public String getAdpTypeId() {
        return adpTypeId;
    }

    public void setAdpTypeId(String adpTypeId) {
        this.adpTypeId = adpTypeId;
    }

    public String getTypeEnDesc() {
        return typeEnDesc;
    }

    public void setTypeEnDesc(String typeEnDesc) {
        this.typeEnDesc = typeEnDesc;
    }

    public String getTypeArDesc() {
        return typeArDesc;
    }

    public void setTypeArDesc(String typeArDesc) {
        this.typeArDesc = typeArDesc;
    }
}
