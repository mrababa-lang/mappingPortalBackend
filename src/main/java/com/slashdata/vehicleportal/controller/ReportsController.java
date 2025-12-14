package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @GetMapping("/export")
    public ApiResponse<String> export(@RequestParam String format,
                                      @RequestParam String scope) {
        return ApiResponse.of("EXPORT_" + format + "_" + scope);
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestParam String interval) {
        return ApiResponse.of(Map.of("interval", interval, "count", 0));
    }
}
