package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ADPMappingHistoryRepository extends JpaRepository<ADPMappingHistory, String> {

    List<ADPMappingHistory> findByAdpMaster_IdOrderByCreatedAtDesc(String adpMasterId);

    void deleteByMapping_Id(String mappingId);

    void deleteByMapping_IdIn(List<String> mappingIds);
}
