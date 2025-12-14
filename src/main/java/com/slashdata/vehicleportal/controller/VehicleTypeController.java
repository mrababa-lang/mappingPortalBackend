package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/types")
@PreAuthorize("isAuthenticated()")
public class VehicleTypeController {

    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeController(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @GetMapping
    public ApiResponse<List<VehicleType>> list() {
        return ApiResponse.of(vehicleTypeRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<VehicleType>> create(@Valid @RequestBody VehicleType vehicleType) {
        if (vehicleTypeRepository.existsByNameIgnoreCase(vehicleType.getName())) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
        return ResponseEntity.ok(ApiResponse.of(vehicleTypeRepository.save(vehicleType)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        vehicleTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
