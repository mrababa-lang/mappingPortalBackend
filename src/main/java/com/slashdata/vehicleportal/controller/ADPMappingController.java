package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpMappingRequest;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.service.AdpMappingService;
import com.slashdata.vehicleportal.specification.ADPMappingSpecifications;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ApiResponse<?> search(@RequestParam(value = "reviewStatus", required = false) String reviewStatus,
                                 @RequestParam(value = "mappingType", required = false) MappingStatus mappingStatus,
                                 @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
                                 @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
                                 Pageable pageable) {
        Specification<ADPMapping> spec = Specification.where(ADPMappingSpecifications.reviewStatus(reviewStatus))
            .and(ADPMappingSpecifications.mappingType(mappingStatus))
            .and(ADPMappingSpecifications.updatedBetween(
                dateFrom != null ? dateFrom.atStartOfDay() : null,
                dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null));
        Page<ADPMapping> page = adpMappingRepository.findAll(spec, pageable);
        return ApiResponse.fromPage(page);
    }

    @PutMapping("/{adpId}")
    public ApiResponse<ADPMapping> upsert(@PathVariable String adpId, @Valid @RequestBody AdpMappingRequest request,
                                          Principal principal) {
        User actor = adpMappingService.findUser(principal != null ? principal.getName() : null);
        return ApiResponse.of(adpMappingService.upsert(adpId, request, actor));
    }

    @GetMapping("/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ApiResponse<?> reviewQueue(@RequestParam(value = "status", required = false) String reviewStatus,
                                      Pageable pageable) {
        Specification<ADPMapping> spec = ADPMappingSpecifications.reviewStatus(reviewStatus);
        return ApiResponse.fromPage(adpMappingRepository.findAll(spec, pageable));
    }

    @PostMapping("/{adpId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> approve(@PathVariable String adpId, Principal principal) {
        ADPMapping mapping = adpMappingRepository.findById(adpId).orElseThrow();
        mapping.setReviewedAt(LocalDateTime.now());
        mapping.setReviewedBy(null);
        if (principal != null) {
            mapping.setReviewedBy(adpMappingService.findUser(principal.getName()));
        }
        adpMappingRepository.save(mapping);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{adpId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> reject(@PathVariable String adpId) {
        adpMappingRepository.deleteById(adpId);
        return ResponseEntity.noContent().build();
    }
}
