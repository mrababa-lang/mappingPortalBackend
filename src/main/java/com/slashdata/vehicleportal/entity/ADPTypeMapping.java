package com.slashdata.vehicleportal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "adp_type_mappings")
public class ADPTypeMapping {

    @Id
    @Column(length = 36, columnDefinition = "char(36)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "adp_type_id", nullable = false, unique = true, length = 50)
    private String adpTypeId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sd_type_id")
    private VehicleType sdType;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdpTypeId() {
        return adpTypeId;
    }

    public void setAdpTypeId(String adpTypeId) {
        this.adpTypeId = adpTypeId;
    }

    public VehicleType getSdType() {
        return sdType;
    }

    public void setSdType(VehicleType sdType) {
        this.sdType = sdType;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
