package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.dto.UniqueAdpMakeView;
import com.slashdata.vehicleportal.dto.UniqueAdpTypeView;
import com.slashdata.vehicleportal.entity.ADPMaster;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ADPMasterRepository extends JpaRepository<ADPMaster, String>, JpaSpecificationExecutor<ADPMaster> {
    Optional<ADPMaster> findByAdpMakeId(String adpMakeId);

    List<ADPMaster> findAllByAdpMakeId(String adpMakeId);

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
        GROUP BY am.adp_make_id, map.sd_make_id, m.name
        """,
        countQuery = """
        SELECT COUNT(DISTINCT am.adp_make_id)
        FROM adp_master am
        WHERE (:q IS NULL OR LOWER(am.make_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_make_id) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
        nativeQuery = true)
    Page<UniqueAdpMakeView> findUniqueMakes(@Param("q") String query, Pageable pageable);

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
        GROUP BY am.adp_type_id, map.sd_type_id, vt.name
        """,
        countQuery = """
        SELECT COUNT(DISTINCT am.adp_type_id)
        FROM adp_master am
        WHERE (:q IS NULL OR LOWER(am.type_en_desc) LIKE LOWER(CONCAT('%', :q, '%')) OR
               LOWER(am.adp_type_id) LIKE LOWER(CONCAT('%', :q, '%')))
        """,
        nativeQuery = true)
    Page<UniqueAdpTypeView> findUniqueTypes(@Param("q") String query, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT am.adpMakeId) FROM ADPMaster am")
    long countDistinctAdpMakeId();

    @Query("SELECT COUNT(DISTINCT am.adpModelId) FROM ADPMaster am")
    long countDistinctAdpModelId();

    @Query("SELECT COUNT(DISTINCT am.adpTypeId) FROM ADPMaster am")
    long countDistinctAdpTypeId();
}
