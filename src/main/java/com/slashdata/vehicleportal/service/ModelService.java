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
import org.springframework.dao.DataIntegrityViolationException;
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

        if (modelRepository.existsById(request.getId())) {
            throw new DataIntegrityViolationException("Model ID already exists");
        }

        Model model = new Model();
        model.setId(request.getId());
        model.setMake(make);
        model.setType(vehicleType);
        model.setName(request.getName());
        model.setNameAr(request.getNameAr());

        return modelRepository.save(model);
    }

    public Model update(Long id, ModelRequest request) {
        Model existingModel = modelRepository.findById(id).orElseThrow();
        if (request.getId() != null && !id.equals(request.getId())) {
            throw new IllegalArgumentException("Model ID cannot be changed");
        }

        Make make = makeRepository.findById(request.getMakeId()).orElseThrow();
        VehicleType vehicleType = vehicleTypeRepository.findById(request.getTypeId()).orElseThrow();

        String normalizedName = request.getName();
        if (normalizedName != null
            && modelRepository.existsByMakeAndNameIgnoreCaseAndIdNot(make, normalizedName, id)) {
            throw new IllegalArgumentException("Model already exists for the provided make");
        }

        existingModel.setMake(make);
        existingModel.setType(vehicleType);
        existingModel.setName(request.getName());
        existingModel.setNameAr(request.getNameAr());
        return modelRepository.save(existingModel);
    }

    @Transactional
    public BulkUploadResult<Model> bulkCreate(List<ModelRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0,
                "No models were uploaded because the payload was empty.", List.of("No model records were provided."));
        }

        List<Model> models = new ArrayList<>();
        Set<String> seenModels = new HashSet<>();
        Set<Long> seenIds = new HashSet<>();
        Map<String, Make> makeCache = new HashMap<>();
        Map<Long, VehicleType> vehicleTypeCache = new HashMap<>();
        int skipped = 0;
        int missingFields = 0;
        int duplicateModels = 0;
        int duplicateIds = 0;

        List<String> skipReasons = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            ModelRequest request = requests.get(i);
            int recordNumber = i + 1;

            List<String> missingFieldNames = new ArrayList<>();
            if (request == null || request.getId() == null) {
                missingFieldNames.add("id");
            }
            if (request == null || request.getMakeId() == null) {
                missingFieldNames.add("makeId");
            }
            if (request == null || request.getTypeId() == null) {
                missingFieldNames.add("typeId");
            }
            if (request == null || request.getName() == null || request.getName().trim().isEmpty()) {
                missingFieldNames.add("name");
            }

            if (!missingFieldNames.isEmpty()) {
                skipped++;
                missingFields++;
                skipReasons.add(String.format("Record %d missing required field(s): %s", recordNumber,
                    String.join(", ", missingFieldNames)));
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

            if (seenIds.contains(request.getId()) || modelRepository.existsById(request.getId())) {
                skipped++;
                duplicateIds++;
                continue;
            }

            Model model = new Model();
            model.setId(request.getId());
            model.setMake(make);
            model.setType(vehicleType);
            model.setName(normalizedName);
            model.setNameAr(request.getNameAr());
            models.add(model);
            seenModels.add(uniqueKey);
            seenIds.add(request.getId());
        }

        List<Model> savedModels = models.isEmpty() ? List.of() : modelRepository.saveAll(models);

        List<String> reasons = new ArrayList<>(skipReasons);
        if (duplicateModels > 0) {
            reasons.add(String.format("%d record(s) skipped because the model already exists for the make.", duplicateModels));
        }
        if (missingFields > 0) {
            reasons.add(String.format("%d record(s) skipped because required fields were missing.", missingFields));
        }
        if (duplicateIds > 0) {
            reasons.add(String.format("%d record(s) skipped because the model ID already exists.", duplicateIds));
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
    public void deleteModel(Long id) {
        Model model = modelRepository.findById(id).orElseThrow();
        adpMappingRepository.clearModelsFromMappings(java.util.List.of(model.getId()), LocalDateTime.now());
        modelRepository.delete(model);
    }
}
