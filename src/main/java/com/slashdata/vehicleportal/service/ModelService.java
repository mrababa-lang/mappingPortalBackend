package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModelService {

    private final ModelRepository modelRepository;
    private final ADPMappingRepository adpMappingRepository;

    public ModelService(ModelRepository modelRepository, ADPMappingRepository adpMappingRepository) {
        this.modelRepository = modelRepository;
        this.adpMappingRepository = adpMappingRepository;
    }

    @Transactional
    public void deleteModel(Long id) {
        Model model = modelRepository.findById(id).orElseThrow();
        adpMappingRepository.clearModelsFromMappings(java.util.List.of(model.getId()), LocalDateTime.now());
        modelRepository.delete(model);
    }
}
