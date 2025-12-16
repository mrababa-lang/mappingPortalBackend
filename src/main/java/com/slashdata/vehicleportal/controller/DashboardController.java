package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.TrendPoint;
import com.slashdata.vehicleportal.dto.RecentActivityDto;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public DashboardController(ADPMasterRepository adpMasterRepository,
                               ADPMappingRepository adpMappingRepository,
                               MakeRepository makeRepository,
                               ModelRepository modelRepository,
                               VehicleTypeRepository vehicleTypeRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        long totalMakes = makeRepository.count();
        long totalModels = modelRepository.count();
        long totalTypes = vehicleTypeRepository.count();
        long adpTotal = adpMasterRepository.count();
        long mappedCount = adpMappingRepository.count();
        long unmappedCount = Math.max(0, adpTotal - mappedCount);
        double adpCoveragePct = adpTotal == 0 ? 0.0 : (mappedCount * 100.0 / adpTotal);
        double localizationScore = mappedCount + unmappedCount == 0 ? 0.0
            : (double) mappedCount / (mappedCount + unmappedCount) * 100.0;

        Map<String, Object> response = new HashMap<>();
        response.put("totalMakes", totalMakes);
        response.put("totalModels", totalModels);
        response.put("totalTypes", totalTypes);
        response.put("adpCoveragePct", adpCoveragePct);
        response.put("mappedCount", mappedCount);
        response.put("unmappedCount", unmappedCount);
        response.put("localizationScore", localizationScore);
        return ApiResponse.of(response);
    }

    @GetMapping("/activity")
    public ApiResponse<List<RecentActivityDto>> activity() {
        List<RecentActivityDto> logs = adpMappingRepository.findTop10ByOrderByUpdatedAtDesc().stream()
            .map(RecentActivityDto::from)
            .collect(Collectors.toList());
        return ApiResponse.of(logs);
    }

    @GetMapping("/trends")
    public ApiResponse<List<TrendPoint>> trends(@RequestParam(value = "from", required = false) LocalDate from,
                                                @RequestParam(value = "to", required = false) LocalDate to) {
        LocalDateTime fromDate = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDate = to != null ? to.plusDays(1).atStartOfDay() : null;
        List<TrendPoint> points = adpMappingRepository.aggregateByDate(fromDate, toDate).stream()
            .map(row -> new TrendPoint(String.valueOf(row[0]), ((Number) row[1]).longValue()))
            .collect(Collectors.toList());
        return ApiResponse.of(points);
    }
}
