package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ADPMappingRepository extends JpaRepository<ADPMapping, Long>, JpaSpecificationExecutor<ADPMapping> {

    Optional<ADPMapping> findByAdpMasterId(Long adpMasterId);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.model = null, m.updatedAt = :updatedAt where m.model.id in :modelIds")
    int clearModelsFromMappings(@Param("modelIds") Iterable<Long> modelIds, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.make = null, m.updatedAt = :updatedAt where m.make.id = :makeId")
    int clearMakeFromMappings(@Param("makeId") Long makeId, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.reviewedAt = CURRENT_TIMESTAMP, m.reviewedBy = :reviewer where m.id in :ids and m.reviewedAt is null")
    int approveAll(@Param("ids") Iterable<Long> ids, @Param("reviewer") String reviewer);

    @Transactional
    @Modifying
    @Query("delete from ADPMapping m where m.id in :ids")
    int deleteAllByIds(@Param("ids") Iterable<Long> ids);
}
