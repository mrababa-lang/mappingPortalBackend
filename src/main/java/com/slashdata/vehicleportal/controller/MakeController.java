package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.service.MakeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/makes")
@PreAuthorize("isAuthenticated()")
public class MakeController {

    private final MakeService makeService;

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    @GetMapping
    public ApiResponse<List<Make>> list() {
        return ApiResponse.of(makeService.findAll());
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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        makeService.deleteMake(id);
        return ResponseEntity.noContent().build();
    }
}
