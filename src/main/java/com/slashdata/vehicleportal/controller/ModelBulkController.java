package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.service.ModelService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models/bulk")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
public class ModelBulkController {

    private final ModelService modelService;

    public ModelBulkController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<List<Model>>> bulkCreate(@RequestBody List<ModelRequest> requests) {
        return ResponseEntity.ok(ApiResponse.of(modelService.bulkCreate(requests)));
    }
}
