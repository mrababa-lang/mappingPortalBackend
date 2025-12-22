package com.slashdata.vehicleportal.dto;

import java.util.Map;

public class AuditPerformanceDto {

    private Map<Long, Long> mappingsPerUser;
    private double accuracyScore;
    private double integrityScore;
    private long totalAiMappings;
    private long approvedAiMappings;

    public AuditPerformanceDto() {
    }

    public AuditPerformanceDto(Map<Long, Long> mappingsPerUser,
                               double accuracyScore,
                               double integrityScore,
                               long totalAiMappings,
                               long approvedAiMappings) {
        this.mappingsPerUser = mappingsPerUser;
        this.accuracyScore = accuracyScore;
        this.integrityScore = integrityScore;
        this.totalAiMappings = totalAiMappings;
        this.approvedAiMappings = approvedAiMappings;
    }

    public Map<Long, Long> getMappingsPerUser() {
        return mappingsPerUser;
    }

    public void setMappingsPerUser(Map<Long, Long> mappingsPerUser) {
        this.mappingsPerUser = mappingsPerUser;
    }

    public double getAccuracyScore() {
        return accuracyScore;
    }

    public void setAccuracyScore(double accuracyScore) {
        this.accuracyScore = accuracyScore;
    }

    public double getIntegrityScore() {
        return integrityScore;
    }

    public void setIntegrityScore(double integrityScore) {
        this.integrityScore = integrityScore;
    }

    public long getTotalAiMappings() {
        return totalAiMappings;
    }

    public void setTotalAiMappings(long totalAiMappings) {
        this.totalAiMappings = totalAiMappings;
    }

    public long getApprovedAiMappings() {
        return approvedAiMappings;
    }

    public void setApprovedAiMappings(long approvedAiMappings) {
        this.approvedAiMappings = approvedAiMappings;
    }
}
