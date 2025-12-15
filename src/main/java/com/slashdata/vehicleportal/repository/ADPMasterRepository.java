package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.dto.AdpMappingViewDto;
import com.slashdata.vehicleportal.entity.MappingStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ADPMasterRepository extends JpaRepository<ADPMaster, String>, JpaSpecificationExecutor<ADPMaster> {
    Optional<ADPMaster> findByAdpMakeId(String adpMakeId);

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
            mapping.updatedAt,
            mapping.reviewedAt
        )
        from ADPMaster master
        left join ADPMapping mapping on mapping.adpMaster = master
        left join Make make on mapping.make = make
        left join Model model on mapping.model = model
        left join User updater on mapping.updatedBy = updater
        where (:query is null or lower(master.makeEnDesc) like lower(concat('%', :query, '%'))
            or lower(master.modelEnDesc) like lower(concat('%', :query, '%')))
          and (:unmappedOnly = false or mapping.id is null)
          and (:status is null or mapping.status = :status)
          and (:userId is null or mapping.updatedBy.id = :userId)
        """)
    Page<AdpMappingViewDto> findMappingViews(@Param("query") String query,
                                              @Param("status") MappingStatus status,
                                              @Param("unmappedOnly") boolean unmappedOnly,
                                              @Param("userId") Long userId,
                                              Pageable pageable);
}
