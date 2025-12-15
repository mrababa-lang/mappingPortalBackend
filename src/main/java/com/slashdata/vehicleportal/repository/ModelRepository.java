package com.slashdata.vehicleportal.repository;

import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepository extends JpaRepository<Model, String> {
    List<Model> findByMake(Make make);

    boolean existsByMakeAndNameIgnoreCase(Make make, String name);

    boolean existsByMakeAndNameIgnoreCaseAndIdNot(Make make, String name, String id);
}
