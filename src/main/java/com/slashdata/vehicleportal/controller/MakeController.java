package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.service.MakeService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/makes")
public class MakeController {

    private final MakeService makeService;

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    @GetMapping
    public PagedResponse<Make> list(@RequestParam(value = "q", required = false) String query,
                                    @RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "dateFrom", required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                    @RequestParam(value = "dateTo", required = false)
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                    @PageableDefault(page = 0, size = 20) Pageable pageable) {
        String normalizedStatus = normalizeStatus(status);
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;
        Page<Make> page = makeService.search(normalizeQuery(query), normalizedStatus, from, to, pageable);
        return PagedResponse.fromPage(page);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Make>> create(@Valid @RequestBody Make make) {
        try {
            return ResponseEntity.ok(ApiResponse.of(makeService.create(make)));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Make>> update(@PathVariable String id, @Valid @RequestBody Make make) {
        try {
            return ResponseEntity.ok(ApiResponse.of(makeService.update(id, make)));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        makeService.deleteMake(id);
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
