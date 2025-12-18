package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.Make;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MakeRepository extends JpaRepository<Make, String> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    java.util.Optional<Make> findByNameIgnoreCase(String name);
}
