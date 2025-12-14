package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.specification.ADPMasterSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adp/master")
public class ADPMasterController {

    private final ADPMasterRepository adpMasterRepository;

    public ADPMasterController(ADPMasterRepository adpMasterRepository) {
        this.adpMasterRepository = adpMasterRepository;
    }

    @GetMapping
    public ApiResponse<?> search(@RequestParam(value = "q", required = false) String query, Pageable pageable) {
        Specification<ADPMaster> spec = ADPMasterSpecifications.textSearch(query);
        Page<ADPMaster> page = adpMasterRepository.findAll(spec, pageable);
        return ApiResponse.fromPage(page);
    }
}
