package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> dbStatus = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            dbStatus.put("status", "up");
            dbStatus.put("url", metaData.getURL());
            dbStatus.put("user", metaData.getUserName());
            dbStatus.put("product", metaData.getDatabaseProductName());
            dbStatus.put("productVersion", metaData.getDatabaseProductVersion());
            dbStatus.put("driverName", metaData.getDriverName());
            dbStatus.put("driverVersion", metaData.getDriverVersion());
        } catch (SQLException exception) {
            dbStatus.put("status", "down");
            dbStatus.put("error", exception.getMessage());
        }

        payload.put("status", "up".equals(dbStatus.get("status")) ? "ok" : "error");
        payload.put("timestamp", Instant.now().toString());
        payload.put("database", dbStatus);
        return ApiResponse.of(payload);
    }
}
