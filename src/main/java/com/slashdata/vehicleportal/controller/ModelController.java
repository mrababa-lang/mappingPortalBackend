package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.service.ModelService;
import com.slashdata.vehicleportal.service.UserLookupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.Principal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/models")
public class ModelController {

    private final ModelRepository modelRepository;
    private final ModelService modelService;
    private final UserLookupService userLookupService;

    public ModelController(ModelRepository modelRepository, ModelService modelService,
                           UserLookupService userLookupService) {
        this.modelRepository = modelRepository;
        this.modelService = modelService;
        this.userLookupService = userLookupService;
    }

    @GetMapping
    public PagedResponse<Model> list(@RequestParam(value = "q", required = false) String query,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "makeId", required = false) String makeId,
                                     @RequestParam(value = "typeId", required = false) Long typeId,
                                     @RequestParam(value = "dateFrom", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                     @RequestParam(value = "dateTo", required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                     @PageableDefault(page = 0, size = 20) Pageable pageable) {
        String normalizedStatus = normalizeStatus(status);
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        Page<Model> page = modelRepository.searchModels(normalizeQuery(query), makeId, typeId, normalizedStatus,
            from, to, pageable);
        return PagedResponse.fromPage(page);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Model>> create(@Valid @RequestBody ModelRequest request,
                                                     Principal principal,
                                                     HttpServletRequest httpRequest) {
        try {
            User actor = userLookupService.findByPrincipal(principal);
            return ResponseEntity.ok(ApiResponse.of(modelService.create(request, actor,
                AuditRequestContext.from(httpRequest))));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Model>> update(@PathVariable Long id, @Valid @RequestBody ModelRequest request,
                                                     Principal principal,
                                                     HttpServletRequest httpRequest) {
        try {
            User actor = userLookupService.findByPrincipal(principal);
            return ResponseEntity.ok(ApiResponse.of(modelService.update(id, request, actor,
                AuditRequestContext.from(httpRequest))));
        } catch (DataIntegrityViolationException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal, HttpServletRequest httpRequest) {
        User actor = userLookupService.findByPrincipal(principal);
        modelService.deleteModel(id, actor, AuditRequestContext.from(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return "all";
        }
        if ("mapped".equalsIgnoreCase(status)) {
            return "mapped";
        }
        if ("unmapped".equalsIgnoreCase(status)) {
            return "unmapped";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
    }
}
