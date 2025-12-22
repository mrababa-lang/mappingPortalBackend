package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.AuditLogDto;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditLog;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.AuditLogRepository;
import com.slashdata.vehicleportal.service.AuditLogService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp/history")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
public class ADPHistoryController {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final ADPMappingRepository adpMappingRepository;

    public ADPHistoryController(AuditLogRepository auditLogRepository,
                                AuditLogService auditLogService,
                                ADPMappingRepository adpMappingRepository) {
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.adpMappingRepository = adpMappingRepository;
    }

    @GetMapping("/{adpId}")
    public ApiResponse<List<AuditLogDto>> getHistory(@PathVariable String adpId) {
        if (adpId == null || adpId.isBlank() || "undefined".equalsIgnoreCase(adpId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP id is required");
        }

        List<AuditLog> masterLogs = auditLogRepository
            .findByEntityIdAndEntityTypeOrderByTimestampAsc(adpId, AuditEntityType.ADP_MASTER);
        List<AuditLog> mappingLogs = adpMappingRepository
            .findByAdpMasterId(adpId)
            .map(mapping -> auditLogRepository.findByEntityIdAndEntityTypeOrderByTimestampAsc(
                mapping.getId(), AuditEntityType.MAPPING))
            .orElse(List.of());

        List<AuditLog> combined = new java.util.ArrayList<>();
        combined.addAll(masterLogs);
        combined.addAll(mappingLogs);

        return ApiResponse.of(auditLogService.getAuditHistory(combined));
    }
}
