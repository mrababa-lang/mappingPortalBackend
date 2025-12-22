package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMakeMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ADPMakeMappingRepository extends JpaRepository<ADPMakeMapping, String> {
    Optional<ADPMakeMapping> findTopByAdpMakeIdOrderByUpdatedAtDesc(String adpMakeId);

    @Query("SELECT COUNT(DISTINCT m.adpMakeId) FROM ADPMakeMapping m")
    long countDistinctMappedAdpMakeId();
}
