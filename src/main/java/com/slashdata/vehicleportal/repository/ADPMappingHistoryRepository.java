package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ADPMappingHistoryRepository extends JpaRepository<ADPMappingHistory, String> {

    void deleteByMapping_Id(String mappingId);

    void deleteByMapping_IdIn(List<String> mappingIds);
}
