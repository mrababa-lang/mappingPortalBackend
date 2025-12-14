package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {
    boolean existsByNameIgnoreCase(String name);
}
