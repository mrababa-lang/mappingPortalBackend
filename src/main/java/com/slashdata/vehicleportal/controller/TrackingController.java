package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ActivityLogDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracking")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
public class TrackingController {

    private final ADPMappingRepository adpMappingRepository;

    public TrackingController(ADPMappingRepository adpMappingRepository) {
        this.adpMappingRepository = adpMappingRepository;
    }

    @GetMapping("/logs")
    public ApiResponse<List<ActivityLogDto>> logs(@RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String dateFrom,
                                                  @RequestParam(required = false) String dateTo) {
        List<ActivityLogDto> logs = adpMappingRepository.findTop10ByOrderByUpdatedAtDesc().stream()
            .map(ActivityLogDto::from)
            .collect(Collectors.toList());
        return ApiResponse.of(logs);
    }
}
