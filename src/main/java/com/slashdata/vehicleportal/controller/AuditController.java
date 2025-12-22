package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.AuditLogDto;
import com.slashdata.vehicleportal.dto.AuditPerformanceDto;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.entity.AuditSource;
import com.slashdata.vehicleportal.service.AuditLogService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/performance")
    public ApiResponse<AuditPerformanceDto> performance(@RequestParam(value = "dateFrom", required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                        @RequestParam(value = "dateTo", required = false)
                                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        return ApiResponse.of(auditLogService.calculatePerformance(from, to));
    }

    @GetMapping("/logs")
    public PagedResponse<AuditLogDto> logs(@RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "20") int size,
                                           @RequestParam(value = "userId", required = false) Long userId,
                                           @RequestParam(value = "source", required = false) AuditSource source,
                                           @RequestParam(value = "dateFrom", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                           @RequestParam(value = "dateTo", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLogDto> pageResult = auditLogService.searchLogs(userId, source, from, to, pageable);
        return PagedResponse.fromPage(pageResult);
    }
}
