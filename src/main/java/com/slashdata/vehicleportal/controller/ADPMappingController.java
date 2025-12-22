package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpMappingRequest;
import com.slashdata.vehicleportal.dto.AdpMappingViewDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.User;
import java.time.LocalDate;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.service.AdpMappingService;
import com.slashdata.vehicleportal.specification.ADPMappingSpecifications;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp/mappings")
@PreAuthorize("isAuthenticated()")
public class ADPMappingController {

    private final ADPMappingRepository adpMappingRepository;
    private final AdpMappingService adpMappingService;

    public ADPMappingController(ADPMappingRepository adpMappingRepository, AdpMappingService adpMappingService) {
        this.adpMappingRepository = adpMappingRepository;
        this.adpMappingService = adpMappingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    public PagedResponse<AdpMappingViewDto> search(@RequestParam(value = "q", required = false) String query,
                                                   @RequestParam(value = "reviewStatus", required = false, defaultValue = "all")
                                                   String reviewStatus,
                                                   @RequestParam(value = "mappingType", required = false, defaultValue = "all")
                                                   String mappingType,
                                                   @RequestParam(value = "status", required = false) String status,
                                                   @RequestParam(value = "userId", required = false) Long userId,
                                                   @RequestParam(value = "adpMakeId", required = false) String adpMakeId,
                                                   @RequestParam(value = "makeId", required = false) String makeId,
                                                   @RequestParam(value = "typeId", required = false) String typeId,
                                                   @RequestParam(value = "dateFrom", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                   @RequestParam(value = "dateTo", required = false)
                                                   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                   @PageableDefault(page = 0, size = 20) Pageable pageable) {
        String normalizedMappingType = status != null ? status : mappingType;
        boolean unmappedOnly = isUnmapped(normalizedMappingType);
        MappingStatus mappingStatus = parseMappingStatus(normalizedMappingType);
        String normalizedReviewStatus = normalizeReviewStatus(reviewStatus);
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        Page<AdpMappingViewDto> page = adpMappingRepository.findMappingViews(normalizeQuery(query), mappingStatus,
            unmappedOnly, normalizedReviewStatus, userId, normalizeQuery(adpMakeId), normalizeQuery(makeId),
            normalizeQuery(typeId), from, to, pageable);
        return PagedResponse.fromPage(page);
    }

    @PutMapping("/{adpId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER')")
    public ApiResponse<ADPMapping> upsert(@PathVariable String adpId, @Valid @RequestBody AdpMappingRequest request,
                                          Principal principal, HttpServletRequest httpRequest) {
        User actor = adpMappingService.findUser(principal != null ? principal.getName() : null);
        return ApiResponse.of(adpMappingService.upsert(adpId, request, actor, AuditRequestContext.from(httpRequest)));
    }

    @GetMapping("/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public PagedResponse<ADPMapping> reviewQueue(@RequestParam(value = "status", required = false) String reviewStatus,
                                                 @PageableDefault(page = 0, size = 20,
                                                     sort = "updatedAt", direction = Sort.Direction.DESC)
                                                 Pageable pageable) {
        Specification<ADPMapping> spec = ADPMappingSpecifications.reviewStatus(reviewStatus);
        return PagedResponse.fromPage(adpMappingRepository.findAll(spec, pageable));
    }

    @PostMapping("/{adpId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> approve(@PathVariable String adpId, Principal principal, HttpServletRequest httpRequest) {
        if (adpId == null || adpId.isBlank() || "undefined".equalsIgnoreCase(adpId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP mapping id is required");
        }

        User reviewer = adpMappingService.findUser(principal != null ? principal.getName() : null);
        adpMappingService.approve(adpId, reviewer, AuditRequestContext.from(httpRequest));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{adpId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> reject(@PathVariable String adpId, Principal principal, HttpServletRequest httpRequest) {
        rejectMapping(adpId, principal, httpRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{adpId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> rejectViaPost(@PathVariable String adpId, Principal principal,
                                              HttpServletRequest httpRequest) {
        rejectMapping(adpId, principal, httpRequest);
        return ResponseEntity.ok().build();
    }

    private void rejectMapping(String adpId, Principal principal, HttpServletRequest httpRequest) {
        if (adpId == null || adpId.isBlank() || "undefined".equalsIgnoreCase(adpId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP mapping id is required");
        }
        User actor = adpMappingService.findUser(principal != null ? principal.getName() : null);
        adpMappingService.reject(adpId, actor, AuditRequestContext.from(httpRequest));
    }

    private MappingStatus parseMappingStatus(String mappingType) {
        if (mappingType == null || "all".equalsIgnoreCase(mappingType) || isUnmapped(mappingType)) {
            return null;
        }
        try {
            return MappingStatus.valueOf(mappingType.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mapping type value");
        }
    }

    private boolean isUnmapped(String mappingType) {
        return mappingType != null && "unmapped".equalsIgnoreCase(mappingType);
    }

    private String normalizeReviewStatus(String reviewStatus) {
        if (reviewStatus == null) {
            return "all";
        }
        String normalized = reviewStatus.toLowerCase();
        if (!normalized.equals("pending") && !normalized.equals("reviewed") && !normalized.equals("all")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid review status value");
        }
        return normalized;
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
