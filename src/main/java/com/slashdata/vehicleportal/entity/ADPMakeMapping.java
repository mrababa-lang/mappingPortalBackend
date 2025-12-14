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
@Table(name = "adp_make_mappings")
public class ADPMakeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "adp_make_id", nullable = false, unique = true, length = 50)
    private String adpMakeId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sd_make_id")
    private Make sdMake;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

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

    public Make getSdMake() {
        return sdMake;
    }

    public void setSdMake(Make sdMake) {
        this.sdMake = sdMake;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
