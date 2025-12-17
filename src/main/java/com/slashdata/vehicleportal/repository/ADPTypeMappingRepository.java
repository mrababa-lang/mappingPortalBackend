package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPTypeMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ADPTypeMappingRepository extends JpaRepository<ADPTypeMapping, String> {
    Optional<ADPTypeMapping> findByAdpTypeId(String adpTypeId);

    @Query("SELECT COUNT(DISTINCT m.adpTypeId) FROM ADPTypeMapping m")
    long countDistinctMappedAdpTypeId();
}
