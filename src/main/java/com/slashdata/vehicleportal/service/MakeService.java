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

    public List<Make> findAll() {
        return makeRepository.findAll();
    }

    @Transactional
    public BulkUploadResult<Make> bulkSave(List<Make> makes) {
        if (makes == null || makes.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0);
        }

        List<Make> makesToSave = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        int skipped = 0;

        for (Make make : makes) {
            if (make == null || make.getName() == null || make.getName().trim().isEmpty()) {
                skipped++;
                continue;
            }

            String normalizedName = make.getName().trim();
            String normalizedKey = normalizedName.toLowerCase();

            if (seenNames.contains(normalizedKey) || makeRepository.existsByNameIgnoreCase(normalizedName)) {
                skipped++;
                continue;
            }

            Make newMake = new Make();
            newMake.setName(normalizedName);
            newMake.setNameAr(make.getNameAr());
            makesToSave.add(newMake);
            seenNames.add(normalizedKey);
        }

        List<Make> savedMakes = makesToSave.isEmpty() ? List.of() : makeRepository.saveAll(makesToSave);

        return new BulkUploadResult<>(savedMakes, savedMakes.size(), skipped);
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
