package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.AdpMasterBulkSyncResponse;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.service.AdpMasterService;
import com.slashdata.vehicleportal.service.UserLookupService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.security.Principal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp/master")
@PreAuthorize("isAuthenticated()")
public class ADPMasterController {

    private final ADPMasterRepository adpMasterRepository;
    private final AdpMasterService adpMasterService;
    private final UserLookupService userLookupService;

    public ADPMasterController(ADPMasterRepository adpMasterRepository, AdpMasterService adpMasterService,
                               UserLookupService userLookupService) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMasterService = adpMasterService;
        this.userLookupService = userLookupService;
    }

    @GetMapping
    public PagedResponse<ADPMaster> search(@RequestParam(value = "q", required = false) String query,
                                           @RequestParam(value = "status", required = false) String status,
                                           @RequestParam(value = "makeId", required = false) String makeId,
                                           @RequestParam(value = "adpMakeId", required = false) String adpMakeId,
                                           @RequestParam(value = "typeId", required = false) String typeId,
                                           @RequestParam(value = "kindCode", required = false) String kindCode,
                                           @RequestParam(value = "dateFrom", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                           @RequestParam(value = "dateTo", required = false)
                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                           @PageableDefault(page = 0, size = 20) Pageable pageable) {
        String normalizedMakeId = adpMakeId != null ? adpMakeId : makeId;
        String normalizedQuery = normalizeQuery(query);
        String normalizedTypeId = normalizeQuery(typeId);
        String normalizedKindCode = normalizeQuery(kindCode);
        StatusFilters filters = normalizeStatus(status);
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        return PagedResponse.fromPage(adpMasterRepository.searchMasterRecords(
            normalizedQuery,
            normalizeQuery(normalizedMakeId),
            normalizedTypeId,
            normalizedKindCode,
            filters.mappingStatus,
            filters.unmappedOnly,
            from,
            to,
            pageable));
    }

    @PostMapping
    public ApiResponse<ADPMaster> create(@RequestBody ADPMaster request, Principal principal,
                                         HttpServletRequest httpRequest) {
        User actor = userLookupService.findByPrincipal(principal);
        return ApiResponse.of(adpMasterService.create(request, actor, AuditRequestContext.from(httpRequest)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ADPMaster> update(@PathVariable String id, @RequestBody ADPMaster request,
                                         Principal principal, HttpServletRequest httpRequest) {
        User actor = userLookupService.findByPrincipal(principal);
        return ApiResponse.of(adpMasterService.update(id, request, actor, AuditRequestContext.from(httpRequest)));
    }

    @PostMapping("/bulk")
    public ApiResponse<AdpMasterBulkSyncResponse> bulkSync(@RequestBody List<ADPMaster> records,
                                                           Principal principal, HttpServletRequest httpRequest) {
        User actor = userLookupService.findByPrincipal(principal);
        return ApiResponse.of(adpMasterService.bulkSync(records, actor, AuditRequestContext.from(httpRequest)));
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private StatusFilters normalizeStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return new StatusFilters(null, false);
        }
        if ("unmapped".equalsIgnoreCase(status)) {
            return new StatusFilters(null, true);
        }
        try {
            return new StatusFilters(MappingStatus.valueOf(status.toUpperCase()), false);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
        }
    }

    private static class StatusFilters {
        private final MappingStatus mappingStatus;
        private final boolean unmappedOnly;

        private StatusFilters(MappingStatus mappingStatus, boolean unmappedOnly) {
            this.mappingStatus = mappingStatus;
            this.unmappedOnly = unmappedOnly;
        }
    }
}
