package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.Make;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MakeRepository extends JpaRepository<Make, String> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    java.util.Optional<Make> findByNameIgnoreCase(String name);

    @Query(value = """
        select distinct make from Make make
        left join ADPMakeMapping mapping on mapping.sdMake = make
        where (:query is null or lower(make.name) like lower(concat('%', :query, '%'))
            or lower(make.nameAr) like lower(concat('%', :query, '%'))
            or lower(make.id) like lower(concat('%', :query, '%')))
          and (
              :status = 'all'
              or (:status = 'mapped' and mapping.id is not null)
              or (:status = 'unmapped' and mapping.id is null)
          )
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """,
        countQuery = """
        select count(distinct make.id) from Make make
        left join ADPMakeMapping mapping on mapping.sdMake = make
        where (:query is null or lower(make.name) like lower(concat('%', :query, '%'))
            or lower(make.nameAr) like lower(concat('%', :query, '%'))
            or lower(make.id) like lower(concat('%', :query, '%')))
          and (
              :status = 'all'
              or (:status = 'mapped' and mapping.id is not null)
              or (:status = 'unmapped' and mapping.id is null)
          )
          and (:from is null or mapping.updatedAt is null or mapping.updatedAt >= :from)
          and (:to is null or mapping.updatedAt is null or mapping.updatedAt <= :to)
        """)
    Page<Make> searchMakes(@Param("query") String query,
                           @Param("status") String status,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to,
                           Pageable pageable);
}
