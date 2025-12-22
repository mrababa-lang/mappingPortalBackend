package com.slashdata.vehicleportal.dto;

import com.slashdata.vehicleportal.entity.AuditAction;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditSource;
import java.time.LocalDateTime;
import java.util.List;

public class AuditLogDto {

    private String id;
    private AuditEntityType entityType;
    private String entityId;
    private AuditAction action;
    private AuditSource source;
    private Long userId;
    private String userFullName;
    private String oldValues;
    private String newValues;
    private List<AuditLogChangeDto> changes;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    public AuditLogDto() {
    }

    public AuditLogDto(String id,
                       AuditEntityType entityType,
                       String entityId,
                       AuditAction action,
                       AuditSource source,
                       Long userId,
                       String userFullName,
                       String oldValues,
                       String newValues,
                       List<AuditLogChangeDto> changes,
                       String ipAddress,
                       String userAgent,
                       LocalDateTime timestamp) {
        this.id = id;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.source = source;
        this.userId = userId;
        this.userFullName = userFullName;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.changes = changes;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(AuditEntityType entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public AuditSource getSource() {
        return source;
    }

    public void setSource(AuditSource source) {
        this.source = source;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getOldValues() {
        return oldValues;
    }

    public void setOldValues(String oldValues) {
        this.oldValues = oldValues;
    }

    public String getNewValues() {
        return newValues;
    }

    public void setNewValues(String newValues) {
        this.newValues = newValues;
    }

    public List<AuditLogChangeDto> getChanges() {
        return changes;
    }

    public void setChanges(List<AuditLogChangeDto> changes) {
        this.changes = changes;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
