package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.service.ModelService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ApiResponse<Model> create(@Valid @RequestBody Model model) {
        return ApiResponse.of(modelRepository.save(model));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
