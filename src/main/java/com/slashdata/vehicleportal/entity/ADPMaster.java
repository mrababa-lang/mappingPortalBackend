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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "adp_id", unique = true)
    private String adpId;

    @Column(name = "make_reference")
    private String adpMakeId;

    @Column(name = "model_reference")
    private String adpModelId;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdpId() {
        return adpId;
    }

    public void setAdpId(String adpId) {
        this.adpId = adpId;
    }

    public String getAdpMakeId() {
        return adpMakeId;
    }

    public void setAdpMakeId(String adpMakeId) {
        this.adpMakeId = adpMakeId;
    }

    public String getAdpModelId() {
        return adpModelId;
    }

    public void setAdpModelId(String adpModelId) {
        this.adpModelId = adpModelId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
