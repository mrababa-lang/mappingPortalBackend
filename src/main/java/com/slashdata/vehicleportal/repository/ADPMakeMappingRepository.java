package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMakeMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ADPMakeMappingRepository extends JpaRepository<ADPMakeMapping, String> {
    Optional<ADPMakeMapping> findByAdpMakeId(String adpMakeId);
}
