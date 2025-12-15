package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public BulkUploadResult<Model> bulkCreate(List<ModelRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0,
                "No models were uploaded because the payload was empty.", List.of("No model records were provided."));
        }

        List<Model> models = new ArrayList<>();
        Set<String> seenModels = new HashSet<>();
        Map<Long, Make> makeCache = new HashMap<>();
        Map<String, VehicleType> vehicleTypeCache = new HashMap<>();
        int skipped = 0;
        int missingFields = 0;
        int duplicateModels = 0;

        for (ModelRequest request : requests) {
            if (request == null || request.getName() == null || request.getName().trim().isEmpty()
                || request.getMakeId() == null || request.getTypeId() == null) {
                skipped++;
                missingFields++;
                continue;
            }

            Make make = makeCache.computeIfAbsent(request.getMakeId(), id -> makeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Make not found for id: " + id)));
            VehicleType vehicleType = vehicleTypeCache.computeIfAbsent(request.getTypeId(), id -> vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle type not found for id: " + id)));

            String normalizedName = request.getName().trim();
            String uniqueKey = make.getId() + "|" + normalizedName.toLowerCase();

            if (seenModels.contains(uniqueKey) || modelRepository.existsByMakeAndNameIgnoreCase(make, normalizedName)) {
                skipped++;
                duplicateModels++;
                continue;
            }

            Model model = new Model();
            model.setMake(make);
            model.setType(vehicleType);
            model.setName(normalizedName);
            model.setNameAr(request.getNameAr());
            models.add(model);
            seenModels.add(uniqueKey);
        }

        List<Model> savedModels = models.isEmpty() ? List.of() : modelRepository.saveAll(models);

        List<String> reasons = new ArrayList<>();
        if (duplicateModels > 0) {
            reasons.add(String.format("%d record(s) skipped because the model already exists for the make.", duplicateModels));
        }
        if (missingFields > 0) {
            reasons.add(String.format("%d record(s) skipped because required fields were missing.", missingFields));
        }

        String message;
        if (savedModels.isEmpty()) {
            message = "No models were uploaded.";
            if (!reasons.isEmpty()) {
                message += " All records were skipped.";
            }
        } else {
            message = String.format("Uploaded %d model(s).", savedModels.size());
            if (skipped > 0) {
                message += String.format(" %d record(s) were skipped.", skipped);
            }
        }

        return new BulkUploadResult<>(savedModels, savedModels.size(), skipped, message, reasons);
    }

    @Transactional
    public void deleteModel(String id) {
        Model model = modelRepository.findById(id).orElseThrow();
        adpMappingRepository.clearModelsFromMappings(java.util.List.of(model.getId()), LocalDateTime.now());
        modelRepository.delete(model);
    }
}
