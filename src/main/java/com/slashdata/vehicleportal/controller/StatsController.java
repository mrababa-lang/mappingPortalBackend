package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.repository.ADPMakeMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.ADPTypeMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMakeMappingRepository adpMakeMappingRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final ADPTypeMappingRepository adpTypeMappingRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public StatsController(ADPMasterRepository adpMasterRepository,
                           ADPMakeMappingRepository adpMakeMappingRepository,
                           ADPMappingRepository adpMappingRepository,
                           ADPTypeMappingRepository adpTypeMappingRepository,
                           MakeRepository makeRepository,
                           ModelRepository modelRepository,
                           VehicleTypeRepository vehicleTypeRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMakeMappingRepository = adpMakeMappingRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.adpTypeMappingRepository = adpTypeMappingRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Long>> dashboard() {
        long totalMakes = makeRepository.count();
        long totalModels = modelRepository.count();
        long totalTypes = vehicleTypeRepository.count();
        long adpTotalMakes = adpMasterRepository.countDistinctAdpMakeId();
        long adpTotalModels = adpMasterRepository.countDistinctAdpModelId();
        long adpTotalTypes = adpMasterRepository.countDistinctAdpTypeId();
        long adpMappedMakes = adpMakeMappingRepository.countDistinctMappedAdpMakeId();
        long adpMappedTypes = adpTypeMappingRepository.countDistinctMappedAdpTypeId();
        long adpTotal = adpMasterRepository.count();
        long mappedCount = adpMappingRepository.countByStatus(MappingStatus.MAPPED);
        long missingModelCount = adpMappingRepository.countByStatus(MappingStatus.MISSING_MODEL);
        long missingMakeCount = adpMappingRepository.countByStatus(MappingStatus.MISSING_MAKE);
        long unmappedCount = Math.max(0, adpTotal - mappedCount - missingModelCount - missingMakeCount);

        double localizationScore = (mappedCount + unmappedCount) == 0
            ? 0L
            : Math.round((double) mappedCount / (mappedCount + unmappedCount) * 100);

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalMakes", totalMakes);
        stats.put("totalModels", totalModels);
        stats.put("totalTypes", totalTypes);
        stats.put("adpTotalMakes", adpTotalMakes);
        stats.put("adpTotalModels", adpTotalModels);
        stats.put("adpTotalTypes", adpTotalTypes);
        stats.put("adpMappedMakes", adpMappedMakes);
        stats.put("adpMappedTypes", adpMappedTypes);
        stats.put("mappedCount", mappedCount);
        stats.put("unmappedCount", unmappedCount);
        stats.put("missingModelCount", missingModelCount);
        stats.put("missingMakeCount", missingMakeCount);
        stats.put("localizationScore", (long) localizationScore);
        return ApiResponse.of(stats);
    }
}
