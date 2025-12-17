package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.service.ModelService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelRepository modelRepository;
    private final ModelService modelService;

    public ModelController(ModelRepository modelRepository, ModelService modelService) {
        this.modelRepository = modelRepository;
        this.modelService = modelService;
    }

    @GetMapping
    public ApiResponse<List<Model>> list() {
        return ApiResponse.of(modelRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Model>> create(@Valid @RequestBody ModelRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.of(modelService.create(request)));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<ApiResponse<Model>> update(@PathVariable Long id, @Valid @RequestBody ModelRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.of(modelService.update(id, request)));
        } catch (DataIntegrityViolationException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.of(null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
