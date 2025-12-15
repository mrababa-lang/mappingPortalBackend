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
@Table(name = "adp_mapping_history")
public class ADPMappingHistory {

    @Id
    @Column(length = 36, columnDefinition = "char(36)")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "adp_id")
    private ADPMaster adpMaster;

    @ManyToOne
    @JoinColumn(name = "mapping_id")
    private ADPMapping mapping;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(length = 500)
    private String details;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ADPMaster getAdpMaster() {
        return adpMaster;
    }

    public void setAdpMaster(ADPMaster adpMaster) {
        this.adpMaster = adpMaster;
    }

    public ADPMapping getMapping() {
        return mapping;
    }

    public void setMapping(ADPMapping mapping) {
        this.mapping = mapping;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
