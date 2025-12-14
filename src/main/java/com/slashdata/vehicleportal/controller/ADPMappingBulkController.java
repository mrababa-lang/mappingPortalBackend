package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.BulkActionRequest;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adp/mappings/bulk-action")
public class ADPMappingBulkController {

    private final ADPMappingRepository adpMappingRepository;

    public ADPMappingBulkController(ADPMappingRepository adpMappingRepository) {
        this.adpMappingRepository = adpMappingRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> bulk(@RequestBody BulkActionRequest request, Principal principal) {
        switch (request.getAction()) {
            case APPROVE -> adpMappingRepository.approveAll(request.getIds(), principal != null ? principal.getName() : "system");
            case REJECT -> adpMappingRepository.deleteAllByIds(request.getIds());
            default -> {
            }
        }
        return ResponseEntity.ok(ApiResponse.of("OK"));
    }
}
