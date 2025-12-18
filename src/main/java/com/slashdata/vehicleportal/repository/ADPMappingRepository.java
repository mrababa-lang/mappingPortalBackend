package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.dto.AdpMappingViewDto;
import com.slashdata.vehicleportal.dto.MappedVehicleExportRow;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ADPMappingRepository extends JpaRepository<ADPMapping, String>, JpaSpecificationExecutor<ADPMapping> {

    Optional<ADPMapping> findByAdpMasterId(String adpMasterId);

    List<ADPMapping> findByAdpMaster_IdIn(Iterable<String> adpMasterIds);

    long countByStatus(MappingStatus status);

    List<ADPMapping> findTop10ByOrderByUpdatedAtDesc();

    @Query("""
        select new com.slashdata.vehicleportal.dto.AdpMappingViewDto(
            master.id,
            master.makeEnDesc,
            master.modelEnDesc,
            master.typeEnDesc,
            mapping.status,
            make.id,
            make.name,
            model.id,
            model.name,
            updater.id,
            updater.fullName,
            mapping.updatedAt,
            mapping.reviewedAt,
            reviewer.id,
            reviewer.fullName
        )
        from ADPMaster master
        left join ADPMapping mapping on mapping.adpMaster = master
        left join mapping.make make
        left join mapping.model model
        left join mapping.updatedBy updater
        left join mapping.reviewedBy reviewer
        where (:query is null or lower(master.makeEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.modelEnDesc) like lower(concat('%', :query, '%')))
          and (
              (:unmappedOnly = true and mapping.id is null)
              or (:unmappedOnly = false and (:mappingStatus is null or mapping.status = :mappingStatus))
          )
          and (
              :reviewStatus = 'all'
              or (mapping.id is null and :unmappedOnly = true)
              or (mapping.id is not null and (
                  (:reviewStatus = 'pending' and mapping.reviewedAt is null)
                  or (:reviewStatus = 'reviewed' and mapping.reviewedAt is not null)
              ))
          )
          and (:userId is null or mapping.updatedBy.id = :userId)
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """)
    Page<AdpMappingViewDto> findMappingViews(@Param("query") String query,
                                             @Param("mappingStatus") MappingStatus mappingStatus,
                                             @Param("unmappedOnly") boolean unmappedOnly,
                                             @Param("reviewStatus") String reviewStatus,
                                             @Param("userId") Long userId,
                                             @Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             Pageable pageable);

    @Query("""
        select new com.slashdata.vehicleportal.dto.AdpMappingViewDto(
            master.id,
            master.makeEnDesc,
            master.modelEnDesc,
            master.typeEnDesc,
            mapping.status,
            make.id,
            make.name,
            model.id,
            model.name,
            updater.id,
            updater.fullName,
            mapping.updatedAt,
            mapping.reviewedAt,
            reviewer.id,
            reviewer.fullName
        )
        from ADPMapping mapping
        join mapping.adpMaster master
        left join mapping.make make
        left join mapping.model model
        left join mapping.updatedBy updater
        left join mapping.reviewedBy reviewer
        where mapping.status in :statuses
          and (:query is null or lower(master.makeEnDesc) like lower(concat('%', :query, '%'))
              or lower(master.modelEnDesc) like lower(concat('%', :query, '%')))
          and (:reviewedOnly = false or mapping.reviewedAt is not null)
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """)
    Page<AdpMappingViewDto> findMappedVehicleViews(@Param("query") String query,
                                                   @Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to,
                                                   @Param("reviewedOnly") boolean reviewedOnly,
                                                   @Param("statuses") Collection<MappingStatus> statuses,
                                                   Pageable pageable);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.model = null, m.updatedAt = :updatedAt where m.model.id in :modelIds")
    int clearModelsFromMappings(@Param("modelIds") Iterable<Long> modelIds, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.status = 'MAPPED', m.make = null, m.updatedAt = :updatedAt where m.make.id = :makeId")
    int clearMakeFromMappings(@Param("makeId") String makeId, @Param("updatedAt") LocalDateTime updatedAt);

    @Transactional
    @Modifying
    @Query("update ADPMapping m set m.reviewedAt = CURRENT_TIMESTAMP, m.reviewedBy = :reviewer where m.id in :ids")
    int approveAll(@Param("ids") Iterable<String> ids, @Param("reviewer") User reviewer);

    @Transactional
    @Modifying
    @Query("delete from ADPMapping m where m.id in :ids")
    int deleteAllByIds(@Param("ids") Iterable<String> ids);

    @Query(value = "SELECT DATE(updated_at) as date, COUNT(*) as cnt FROM adp_mappings "
        + "WHERE (:from is null or updated_at >= :from) and (:to is null or updated_at <= :to) "
        + "GROUP BY DATE(updated_at) ORDER BY DATE(updated_at)", nativeQuery = true)
    List<Object[]> aggregateByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
        select new com.slashdata.vehicleportal.dto.MappedVehicleExportRow(
            master.id,
            master.makeEnDesc,
            master.modelEnDesc,
            master.typeEnDesc,
            make.name,
            model.name,
            type.name,
            mapping.status,
            updater.fullName,
            mapping.updatedAt
        )
        from ADPMapping mapping
        join mapping.adpMaster master
        left join mapping.make make
        left join mapping.model model
        left join model.type type
        left join mapping.updatedBy updater
        where mapping.status in :statuses
          and (:reviewedOnly = false or mapping.reviewedAt is not null)
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        order by master.makeEnDesc, master.modelEnDesc
        """)
    Stream<MappedVehicleExportRow> streamMappedVehiclesForExport(@Param("statuses") Collection<MappingStatus> statuses,
                                                                 @Param("from") LocalDateTime from,
                                                                 @Param("to") LocalDateTime to,
                                                                 @Param("reviewedOnly") boolean reviewedOnly);
}
