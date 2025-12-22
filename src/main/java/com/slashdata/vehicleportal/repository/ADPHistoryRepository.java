package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ADPHistoryRepository extends JpaRepository<ADPHistory, String> {

    List<ADPHistory> findByAdpMaster_IdOrderByCreatedAtDesc(String adpMasterId);
}
