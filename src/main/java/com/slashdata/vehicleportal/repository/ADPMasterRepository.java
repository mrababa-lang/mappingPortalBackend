package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMaster;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ADPMasterRepository extends JpaRepository<ADPMaster, String>, JpaSpecificationExecutor<ADPMaster> {
    Optional<ADPMaster> findByAdpMakeId(String adpMakeId);
}
