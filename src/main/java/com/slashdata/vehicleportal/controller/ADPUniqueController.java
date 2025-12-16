package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpMakeMapRequest;
import com.slashdata.vehicleportal.dto.AdpTypeMapRequest;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMakeMapping;
import com.slashdata.vehicleportal.entity.ADPTypeMapping;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.ADPMakeMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.ADPTypeMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp")
@PreAuthorize("isAuthenticated()")
public class ADPUniqueController {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMakeMappingRepository adpMakeMappingRepository;
    private final ADPTypeMappingRepository adpTypeMappingRepository;
    private final MakeRepository makeRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public ADPUniqueController(ADPMasterRepository adpMasterRepository,
                               ADPMakeMappingRepository adpMakeMappingRepository,
                               ADPTypeMappingRepository adpTypeMappingRepository,
                               MakeRepository makeRepository,
                               VehicleTypeRepository vehicleTypeRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMakeMappingRepository = adpMakeMappingRepository;
        this.adpTypeMappingRepository = adpTypeMappingRepository;
        this.makeRepository = makeRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @GetMapping("/makes")
    public ApiResponse<?> uniqueMakes(@RequestParam(value = "q", required = false) String query, Pageable pageable) {
        return ApiResponse.fromPage(adpMasterRepository.findUniqueMakes(query, pageable));
    }

    @PostMapping("/makes/map")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ApiResponse<ADPMakeMapping> mapMake(@Valid @RequestBody AdpMakeMapRequest request) {
        Make sdMake = makeRepository.findById(request.getSdMakeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SD make not found"));

        ADPMakeMapping mapping = adpMakeMappingRepository.findByAdpMakeId(request.getAdpMakeId())
            .orElseGet(ADPMakeMapping::new);
        mapping.setAdpMakeId(request.getAdpMakeId());
        mapping.setSdMake(sdMake);
        mapping.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.of(adpMakeMappingRepository.save(mapping));
    }

    @GetMapping("/types")
    public ApiResponse<?> uniqueTypes(@RequestParam(value = "q", required = false) String query, Pageable pageable) {
        return ApiResponse.fromPage(adpMasterRepository.findUniqueTypes(query, pageable));
    }

    @PostMapping("/types/map")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ApiResponse<ADPTypeMapping> mapType(@Valid @RequestBody AdpTypeMapRequest request) {
        VehicleType sdType = vehicleTypeRepository.findById(request.getSdTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SD vehicle type not found"));

        ADPTypeMapping mapping = adpTypeMappingRepository.findByAdpTypeId(request.getAdpTypeId())
            .orElseGet(ADPTypeMapping::new);
        mapping.setAdpTypeId(request.getAdpTypeId());
        mapping.setSdType(sdType);
        mapping.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.of(adpTypeMappingRepository.save(mapping));
    }
}
