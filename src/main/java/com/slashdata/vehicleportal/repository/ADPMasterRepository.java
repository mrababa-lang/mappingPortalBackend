package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.dto.UniqueAdpMakeView;
import com.slashdata.vehicleportal.dto.UniqueAdpTypeView;
import com.slashdata.vehicleportal.dto.AdpMakeExportRow;
import com.slashdata.vehicleportal.dto.AdpTypeExportRow;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.MappingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ADPMasterRepository extends JpaRepository<ADPMaster, String>, JpaSpecificationExecutor<ADPMaster> {
    Optional<ADPMaster> findByAdpMakeId(String adpMakeId);

    List<ADPMaster> findAllByAdpMakeId(String adpMakeId);

    Optional<ADPMaster> findByAdpMakeIdAndAdpModelId(String adpMakeId, String adpModelId);

    @Query(value = """
        SELECT am.adp_make_id AS adpMakeId,
               MAX(am.make_en_desc) AS adpMakeName,
               map.sd_make_id AS sdMakeId,
               m.name AS sdMakeName
        FROM adp_master am
        LEFT JOIN adp_make_mappings map ON map.adp_make_id = am.adp_make_id
        LEFT JOIN makes m ON m.id = map.sd_make_id
        WHERE (:q IS NULL OR LOWER(am.make_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_make_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_make_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_make_id IS NULL)
          )
        GROUP BY am.adp_make_id, map.sd_make_id, m.name
        """,
        countQuery = """
        SELECT COUNT(DISTINCT am.adp_make_id)
        FROM adp_master am
        LEFT JOIN adp_make_mappings map ON map.adp_make_id = am.adp_make_id
        WHERE (:q IS NULL OR LOWER(am.make_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_make_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_make_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_make_id IS NULL)
          )
        """,
        nativeQuery = true)
    Page<UniqueAdpMakeView> findUniqueMakes(@Param("q") String query, @Param("status") String status, Pageable pageable);

    @Query(value = """
        SELECT am.adp_type_id AS adpTypeId,
               MAX(am.type_en_desc) AS adpTypeName,
               map.sd_type_id AS sdTypeId,
               vt.name AS sdTypeName
        FROM adp_master am
        LEFT JOIN adp_type_mappings map ON map.adp_type_id = am.adp_type_id
        LEFT JOIN vehicle_types vt ON vt.id = map.sd_type_id
        WHERE (:q IS NULL OR LOWER(am.type_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_type_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_type_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_type_id IS NULL)
          )
        GROUP BY am.adp_type_id, map.sd_type_id, vt.name
        """,
        countQuery = """
        SELECT COUNT(DISTINCT am.adp_type_id)
        FROM adp_master am
        LEFT JOIN adp_type_mappings map ON map.adp_type_id = am.adp_type_id
        WHERE (:q IS NULL OR LOWER(am.type_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_type_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_type_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_type_id IS NULL)
          )
        """,
        nativeQuery = true)
    Page<UniqueAdpTypeView> findUniqueTypes(@Param("q") String query, @Param("status") String status, Pageable pageable);

    @Query(value = """
        select distinct master from ADPMaster master
        left join ADPMapping mapping on mapping.adpMaster = master
        where (:query is null or lower(master.adpMakeId) like lower(concat('%', :query, '%'))
            or lower(master.adpModelId) like lower(concat('%', :query, '%'))
            or lower(master.makeEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.modelEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.kindEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.kindCode) like lower(concat('%', :query, '%')))
          and (:adpMakeId is null or master.adpMakeId = :adpMakeId)
          and (:typeId is null or master.adpTypeId = :typeId)
          and (:kindCode is null or master.kindCode = :kindCode)
          and (
              (:unmappedOnly = true and mapping.id is null)
              or (:unmappedOnly = false and (:mappingStatus is null or mapping.status = :mappingStatus))
          )
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """,
        countQuery = """
        select count(distinct master.id) from ADPMaster master
        left join ADPMapping mapping on mapping.adpMaster = master
        where (:query is null or lower(master.adpMakeId) like lower(concat('%', :query, '%'))
            or lower(master.adpModelId) like lower(concat('%', :query, '%'))
            or lower(master.makeEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.modelEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.kindEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.kindCode) like lower(concat('%', :query, '%')))
          and (:adpMakeId is null or master.adpMakeId = :adpMakeId)
          and (:typeId is null or master.adpTypeId = :typeId)
          and (:kindCode is null or master.kindCode = :kindCode)
          and (
              (:unmappedOnly = true and mapping.id is null)
              or (:unmappedOnly = false and (:mappingStatus is null or mapping.status = :mappingStatus))
          )
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """)
    Page<ADPMaster> searchMasterRecords(@Param("query") String query,
                                        @Param("adpMakeId") String adpMakeId,
                                        @Param("typeId") String typeId,
                                        @Param("kindCode") String kindCode,
                                        @Param("mappingStatus") MappingStatus mappingStatus,
                                        @Param("unmappedOnly") boolean unmappedOnly,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        Pageable pageable);

    @Query("SELECT COUNT(DISTINCT am.adpMakeId) FROM ADPMaster am")
    long countDistinctAdpMakeId();

    @Query("SELECT COUNT(DISTINCT am.adpModelId) FROM ADPMaster am")
    long countDistinctAdpModelId();

    @Query("SELECT COUNT(DISTINCT am.adpTypeId) FROM ADPMaster am")
    long countDistinctAdpTypeId();

    @Query(value = """
        SELECT am.adp_make_id AS adpMakeId,
               MAX(am.make_en_desc) AS adpMakeName,
               MAX(am.make_ar_desc) AS adpMakeNameAr,
               map.sd_make_id AS sdMakeId,
               m.name AS sdMakeName
        FROM adp_master am
        LEFT JOIN adp_make_mappings map ON map.adp_make_id = am.adp_make_id
        LEFT JOIN makes m ON m.id = map.sd_make_id
        WHERE (:q IS NULL OR LOWER(am.make_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_make_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_make_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_make_id IS NULL)
          )
        GROUP BY am.adp_make_id, map.sd_make_id, m.name
        ORDER BY am.adp_make_id
        """,
        nativeQuery = true)
    Stream<AdpMakeExportRow> streamUniqueMakesForExport(@Param("q") String query,
                                                       @Param("status") String status);

    @Query(value = """
        SELECT am.adp_type_id AS adpTypeId,
               MAX(am.type_en_desc) AS adpTypeName,
               MAX(am.type_ar_desc) AS adpTypeNameAr,
               map.sd_type_id AS sdTypeId,
               vt.name AS sdTypeName
        FROM adp_master am
        LEFT JOIN adp_type_mappings map ON map.adp_type_id = am.adp_type_id
        LEFT JOIN vehicle_types vt ON vt.id = map.sd_type_id
        WHERE (:q IS NULL OR LOWER(am.type_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_type_id) LIKE LOWER(CONCAT('%', :q, '%')))
          AND (
              :status = 'all'
              OR (:status = 'mapped' AND map.sd_type_id IS NOT NULL)
              OR (:status = 'unmapped' AND map.sd_type_id IS NULL)
          )
        GROUP BY am.adp_type_id, map.sd_type_id, vt.name
        ORDER BY am.adp_type_id
        """,
        nativeQuery = true)
    Stream<AdpTypeExportRow> streamUniqueTypesForExport(@Param("q") String query,
                                                       @Param("status") String status);

    @Query("""
        select master from ADPMaster master
        where not exists (
            select 1 from ADPMapping mapping where mapping.adpMaster.id = master.id
        )
        """)
    List<ADPMaster> findUnmappedRecords();

    @Query("""
        select master from ADPMaster master
        where not exists (
            select 1 from ADPMapping mapping where mapping.adpMaster.id = master.id
        )
        """)
    Page<ADPMaster> findUnmappedRecords(Pageable pageable);
}
