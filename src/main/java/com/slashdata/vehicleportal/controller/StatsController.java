package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final UserRepository userRepository;
    private final ADPMasterRepository adpMasterRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;

    public StatsController(UserRepository userRepository, ADPMasterRepository adpMasterRepository,
                           ADPMappingRepository adpMappingRepository, MakeRepository makeRepository,
                           ModelRepository modelRepository) {
        this.userRepository = userRepository;
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Long>> dashboard() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        stats.put("adpMaster", adpMasterRepository.count());
        stats.put("adpMappings", adpMappingRepository.count());
        stats.put("makes", makeRepository.count());
        stats.put("models", modelRepository.count());
        return ApiResponse.of(stats);
    }
}
