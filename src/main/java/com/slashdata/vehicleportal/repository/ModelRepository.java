package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.dto.MasterVehicleExportRow;
import com.slashdata.vehicleportal.dto.MasterVehicleView;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModelRepository extends JpaRepository<Model, Long> {
    List<Model> findByMake(Make make);

    boolean existsByMakeAndNameIgnoreCase(Make make, String name);

    boolean existsByMakeAndNameIgnoreCaseAndIdNot(Make make, String name, Long id);

    List<Model> findByMakeAndNameIgnoreCase(Make make, String name);

    @Query("""
        select new com.slashdata.vehicleportal.dto.MasterVehicleView(
            model.id,
            model.name,
            model.nameAr,
            make.id,
            make.name,
            make.nameAr,
            type.id,
            type.name,
            max(adp.kindCode),
            max(adp.kindEnDesc),
            max(adp.kindArDesc)
        )
        from Model model
        join model.make make
        join model.type type
        left join ADPMapping mapping on mapping.model = model
            and mapping.status = com.slashdata.vehicleportal.entity.MappingStatus.MAPPED
        left join mapping.adpMaster adp
        where (:query is null or lower(model.name) like lower(concat('%', :query, '%'))
            or lower(make.name) like lower(concat('%', :query, '%')))
          and (:makeId is null or make.id = :makeId)
          and (:typeId is null or type.id = :typeId)
        group by model.id, model.name, model.nameAr, make.id, make.name, make.nameAr, type.id, type.name
        order by make.name, model.name
        """)
    Page<MasterVehicleView> findMasterVehicleViews(@Param("query") String query,
                                                   @Param("makeId") String makeId,
                                                   @Param("typeId") Long typeId,
                                                   Pageable pageable);

    @Query("""
        select new com.slashdata.vehicleportal.dto.MasterVehicleExportRow(
            make.id,
            make.name,
            make.nameAr,
            model.id,
            model.name,
            model.nameAr,
            type.id,
            type.name,
            max(adp.kindCode),
            max(adp.kindEnDesc),
            max(adp.kindArDesc)
        )
        from Model model
        join model.make make
        join model.type type
        left join ADPMapping mapping on mapping.model = model
            and mapping.status = com.slashdata.vehicleportal.entity.MappingStatus.MAPPED
        left join mapping.adpMaster adp
        where (:makeId is null or make.id = :makeId)
          and (:typeId is null or type.id = :typeId)
        group by model.id, model.name, model.nameAr, make.id, make.name, make.nameAr, type.id, type.name
        order by make.name, model.name
        """)
    Stream<MasterVehicleExportRow> streamMasterVehiclesForExport(@Param("makeId") String makeId,
                                                                 @Param("typeId") Long typeId);
}
