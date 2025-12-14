package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.Make;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MakeRepository extends JpaRepository<Make, Long> {
    boolean existsByNameIgnoreCase(String name);
}
