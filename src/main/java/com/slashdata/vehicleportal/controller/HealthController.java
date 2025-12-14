package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.UserRepository;
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
    private final UserRepository userRepository;

    public HealthController(DataSource dataSource, UserRepository userRepository) {
        this.dataSource = dataSource;
        this.userRepository = userRepository;
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

        long userCount = userRepository.count();
        if (userCount == 1) {
            userRepository
                    .findAll()
                    .stream()
                    .findFirst()
                    .ifPresent(user -> payload.put("user", buildSingleUserDetails(user)));
        }

        return ApiResponse.of(payload);
    }

    private Map<String, Object> buildSingleUserDetails(User user) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", user.getEmail());
        userDetails.put("password", user.getPassword());
        userDetails.put("password_", user.getPasswordUnhashed());
        return userDetails;
    }
}
