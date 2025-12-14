package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final MakeRepository makeRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public ModelService(ModelRepository modelRepository, ADPMappingRepository adpMappingRepository,
                       MakeRepository makeRepository, VehicleTypeRepository vehicleTypeRepository) {
        this.modelRepository = modelRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.makeRepository = makeRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    public Model create(ModelRequest request) {
        Make make = makeRepository.findById(request.getMakeId()).orElseThrow();
        VehicleType vehicleType = vehicleTypeRepository.findById(request.getTypeId()).orElseThrow();

        Model model = new Model();
        model.setMake(make);
        model.setType(vehicleType);
        model.setName(request.getName());
        model.setNameAr(request.getNameAr());

        return modelRepository.save(model);
    }

    @Transactional
    public void deleteModel(String id) {
        Model model = modelRepository.findById(id).orElseThrow();
        adpMappingRepository.clearModelsFromMappings(java.util.List.of(model.getId()), LocalDateTime.now());
        modelRepository.delete(model);
    }
}
