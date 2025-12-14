package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.AdpMappingRequest;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdpMappingService {

    private final ADPMappingRepository adpMappingRepository;
    private final ADPMasterRepository adpMasterRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;

    public AdpMappingService(ADPMappingRepository adpMappingRepository, ADPMasterRepository adpMasterRepository,
                             MakeRepository makeRepository, ModelRepository modelRepository,
                             UserRepository userRepository) {
        this.adpMappingRepository = adpMappingRepository;
        this.adpMasterRepository = adpMasterRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ADPMapping upsert(String adpId, AdpMappingRequest request) {
        ADPMaster master = adpMasterRepository.findById(request.getAdpMasterId()).orElseThrow();
        ADPMapping mapping = adpMappingRepository.findByAdpMasterId(master.getId()).orElse(new ADPMapping());
        mapping.setAdpMaster(master);
        if (request.getMakeId() != null) {
            Make make = makeRepository.findById(request.getMakeId()).orElseThrow();
            mapping.setMake(make);
        } else {
            mapping.setMake(null);
        }
        if (request.getModelId() != null) {
            Model model = modelRepository.findById(request.getModelId()).orElseThrow();
            mapping.setModel(model);
        } else {
            mapping.setModel(null);
        }
        mapping.setStatus(request.getStatus());
        mapping.setReviewedAt(null);
        mapping.setReviewedBy(null);
        mapping.setUpdatedAt(LocalDateTime.now());
        return adpMappingRepository.save(mapping);
    }

    public User findUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
