package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.BulkActionRequest;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.service.AdpMappingService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/adp/mappings/bulk-action")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
public class ADPMappingBulkController {

    private final AdpMappingService adpMappingService;

    public ADPMappingBulkController(AdpMappingService adpMappingService) {
        this.adpMappingService = adpMappingService;
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<ApiResponse<String>> bulk(@RequestBody BulkActionRequest request, Principal principal,
                                                    HttpServletRequest httpRequest) {
        User actor = adpMappingService.findUser(principal != null ? principal.getName() : null);
        AuditRequestContext context = AuditRequestContext.from(httpRequest);
        switch (request.getAction()) {
            case APPROVE -> adpMappingService.bulkApprove(request.getIds(), actor, context);
            case REJECT -> adpMappingService.bulkReject(request.getIds(), actor, context);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported bulk action");
        }
        return ResponseEntity.ok(ApiResponse.of("OK"));
    }
}
