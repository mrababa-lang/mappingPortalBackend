package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.AdpMasterBulkSyncResponse;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.service.AdpMasterService;
import com.slashdata.vehicleportal.specification.ADPMasterSpecifications;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adp/master")
@PreAuthorize("isAuthenticated()")
public class ADPMasterController {

    private final ADPMasterRepository adpMasterRepository;
    private final AdpMasterService adpMasterService;

    public ADPMasterController(ADPMasterRepository adpMasterRepository, AdpMasterService adpMasterService) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMasterService = adpMasterService;
    }

    @GetMapping
    public ApiResponse<?> search(@RequestParam(value = "q", required = false) String query, Pageable pageable) {
        Specification<ADPMaster> spec = ADPMasterSpecifications.textSearch(query);
        Page<ADPMaster> page = adpMasterRepository.findAll(spec, pageable);
        return ApiResponse.fromPage(page);
    }

    @PostMapping
    public ApiResponse<ADPMaster> create(@RequestBody ADPMaster request) {
        return ApiResponse.of(adpMasterService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ADPMaster> update(@PathVariable String id, @RequestBody ADPMaster request) {
        return ApiResponse.of(adpMasterService.update(id, request));
    }

    @PostMapping("/bulk")
    public ApiResponse<AdpMasterBulkSyncResponse> bulkSync(@RequestBody List<ADPMaster> records) {
        return ApiResponse.of(adpMasterService.bulkSync(records));
    }
}
