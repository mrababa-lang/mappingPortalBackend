package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ADPMappingRepository extends JpaRepository<ADPMapping, String>, JpaSpecificationExecutor<ADPMapping> {

    Optional<ADPMapping> findByAdpMasterId(String adpMasterId);

    long countByStatus(MappingStatus status);

    List<ADPMapping> findTop10ByOrderByUpdatedAtDesc();

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.model = null, m.updatedAt = :updatedAt where m.model.id in :modelIds")
    int clearModelsFromMappings(@Param("modelIds") Iterable<String> modelIds, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.make = null, m.updatedAt = :updatedAt where m.make.id = :makeId")
    int clearMakeFromMappings(@Param("makeId") Long makeId, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.reviewedAt = CURRENT_TIMESTAMP, m.reviewedBy = :reviewer where m.id in :ids and m.reviewedAt is null")
    int approveAll(@Param("ids") Iterable<String> ids, @Param("reviewer") String reviewer);

    @Transactional
    @Modifying
    @Query("delete from ADPMapping m where m.id in :ids")
    int deleteAllByIds(@Param("ids") Iterable<String> ids);

    @Query(value = "SELECT DATE(updated_at) as date, COUNT(*) as cnt FROM adp_mappings "
        + "WHERE (:from is null or updated_at >= :from) and (:to is null or updated_at <= :to) "
        + "GROUP BY DATE(updated_at) ORDER BY DATE(updated_at)", nativeQuery = true)
    List<Object[]> aggregateByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
