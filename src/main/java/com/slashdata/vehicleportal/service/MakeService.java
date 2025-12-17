package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MakeService {

    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final ADPMappingRepository adpMappingRepository;

    public MakeService(MakeRepository makeRepository, ModelRepository modelRepository, ADPMappingRepository adpMappingRepository) {
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.adpMappingRepository = adpMappingRepository;
    }

    public Make create(Make make) {
        if (makeRepository.existsByNameIgnoreCase(make.getName())) {
            throw new DataIntegrityViolationException("Make name already exists");
        }
        return makeRepository.save(make);
    }

    public Make update(Long id, Make updatedMake) {
        Make existingMake = makeRepository.findById(id).orElseThrow();
        String normalizedName = updatedMake.getName();
        if (normalizedName != null && makeRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DataIntegrityViolationException("Make name already exists");
        }

        existingMake.setName(updatedMake.getName());
        existingMake.setNameAr(updatedMake.getNameAr());
        return makeRepository.save(existingMake);
    }

    public List<Make> findAll() {
        return makeRepository.findAll();
    }

    @Transactional
    public BulkUploadResult<Make> bulkSave(List<Make> makes) {
        if (makes == null || makes.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0,
                "No makes were uploaded because the payload was empty.", List.of("No make records were provided."));
        }

        List<Make> makesToSave = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        int skipped = 0;
        int duplicateNames = 0;
        int invalidNames = 0;

        List<String> skipReasons = new ArrayList<>();

        for (int i = 0; i < makes.size(); i++) {
            Make make = makes.get(i);
            int recordNumber = i + 1;

            List<String> missingFields = new ArrayList<>();
            if (make == null || make.getName() == null || make.getName().trim().isEmpty()) {
                missingFields.add("name");
            }

            if (!missingFields.isEmpty()) {
                skipped++;
                invalidNames++;
                skipReasons.add(String.format("Record %d missing required field(s): %s", recordNumber,
                    String.join(", ", missingFields)));
                continue;
            }

            String normalizedName = make.getName().trim();
            String normalizedKey = normalizedName.toLowerCase();

            if (seenNames.contains(normalizedKey) || makeRepository.existsByNameIgnoreCase(normalizedName)) {
                skipped++;
                duplicateNames++;
                continue;
            }

            Make newMake = new Make();
            newMake.setId(make.getId());
            newMake.setName(normalizedName);
            newMake.setNameAr(make.getNameAr());
            makesToSave.add(newMake);
            seenNames.add(normalizedKey);
        }

        List<Make> savedMakes = makesToSave.isEmpty() ? List.of() : makeRepository.saveAll(makesToSave);

        List<String> reasons = new ArrayList<>(skipReasons);
        if (duplicateNames > 0) {
            reasons.add(String.format("%d record(s) skipped because the make name already exists.", duplicateNames));
        }
        if (invalidNames > 0) {
            reasons.add(String.format("%d record(s) skipped because the make name was missing.", invalidNames));
        }

        String message;
        if (savedMakes.isEmpty()) {
            message = "No makes were uploaded.";
            if (!reasons.isEmpty()) {
                message += " All records were skipped.";
            }
        } else {
            message = String.format("Uploaded %d make(s).", savedMakes.size());
            if (skipped > 0) {
                message += String.format(" %d record(s) were skipped.", skipped);
            }
        }

        return new BulkUploadResult<>(savedMakes, savedMakes.size(), skipped, message, reasons);
    }

    @Transactional
    public void deleteMake(Long id) {
        Make make = makeRepository.findById(id).orElseThrow();
        List<Model> models = modelRepository.findByMake(make);
        LocalDateTime now = LocalDateTime.now();
        if (!models.isEmpty()) {
            adpMappingRepository.clearModelsFromMappings(models.stream().map(Model::getId).toList(), now);
        }
        adpMappingRepository.clearMakeFromMappings(make.getId(), now);
        modelRepository.deleteAll(models);
        makeRepository.delete(make);
    }
}
