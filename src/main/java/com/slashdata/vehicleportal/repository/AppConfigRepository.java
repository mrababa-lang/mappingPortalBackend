package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.AppConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppConfigRepository extends JpaRepository<AppConfig, Long> {
    Optional<AppConfig> findTopByOrderByIdAsc();
}
