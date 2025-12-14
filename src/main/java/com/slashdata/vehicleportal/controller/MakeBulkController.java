package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.service.MakeService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/makes/bulk")
public class MakeBulkController {

    private final MakeService makeService;

    public MakeBulkController(MakeService makeService) {
        this.makeService = makeService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<Make>>> bulkUpsert(@RequestBody List<Make> makes) {
        return ResponseEntity.ok(ApiResponse.of(makeService.bulkSave(makes)));
    }
}
