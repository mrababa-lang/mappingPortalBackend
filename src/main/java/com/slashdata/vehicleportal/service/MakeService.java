package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.AuditAction;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditSource;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MakeService {

    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final AuditLogService auditLogService;

    public MakeService(MakeRepository makeRepository, ModelRepository modelRepository,
                       ADPMappingRepository adpMappingRepository, AuditLogService auditLogService) {
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.auditLogService = auditLogService;
    }

    public Make create(Make make, User actor, AuditRequestContext context) {
        if (makeRepository.existsByNameIgnoreCase(make.getName())) {
            throw new DataIntegrityViolationException("Make name already exists");
        }
        if (makeRepository.existsById(make.getId())) {
            throw new DataIntegrityViolationException("Make ID already exists");
        }
        Make saved = makeRepository.save(make);
        auditLogService.logChange(AuditEntityType.SD_MAKE, saved.getId(), AuditAction.CREATE, AuditSource.MANUAL,
            actor, null, makeSnapshot(saved), context);
        return saved;
    }

    public Make update(String id, Make updatedMake, User actor, AuditRequestContext context) {
        Make existingMake = makeRepository.findById(id).orElseThrow();
        Map<String, Object> oldSnapshot = makeSnapshot(existingMake);
        if (updatedMake.getId() != null && !id.equals(updatedMake.getId())) {
            throw new DataIntegrityViolationException("Make ID cannot be changed");
        }
        String normalizedName = updatedMake.getName();
        if (normalizedName != null && makeRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DataIntegrityViolationException("Make name already exists");
        }

        existingMake.setName(updatedMake.getName());
        existingMake.setNameAr(updatedMake.getNameAr());
        Make saved = makeRepository.save(existingMake);
        auditLogService.logChange(AuditEntityType.SD_MAKE, saved.getId(), AuditAction.UPDATE, AuditSource.MANUAL,
            actor, oldSnapshot, makeSnapshot(saved), context);
        return saved;
    }

    public List<Make> findAll() {
        return makeRepository.findAll();
    }

    public Page<Make> search(String query, String status, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return makeRepository.searchMakes(query, status, from, to, pageable);
    }

    @Transactional
    public BulkUploadResult<Make> bulkSave(List<Make> makes, User actor, AuditRequestContext context) {
        if (makes == null || makes.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0,
                "No makes were uploaded because the payload was empty.", List.of("No make records were provided."));
        }

        List<Make> makesToSave = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        Set<String> seenIds = new HashSet<>();
        int skipped = 0;
        int duplicateNames = 0;
        int duplicateIds = 0;
        int invalidNames = 0;
        int invalidIds = 0;

        List<String> skipReasons = new ArrayList<>();

        for (int i = 0; i < makes.size(); i++) {
            Make make = makes.get(i);
            int recordNumber = i + 1;

            List<String> missingFields = new ArrayList<>();
            if (make == null || make.getId() == null || make.getId().trim().isEmpty()) {
                missingFields.add("id");
            }
            if (make == null || make.getName() == null || make.getName().trim().isEmpty()) {
                missingFields.add("name");
            }

            if (!missingFields.isEmpty()) {
                skipped++;
                if (missingFields.contains("id")) {
                    invalidIds++;
                }
                if (missingFields.contains("name")) {
                    invalidNames++;
                }
                skipReasons.add(String.format("Record %d missing required field(s): %s", recordNumber,
                    String.join(", ", missingFields)));
                continue;
            }

            String normalizedId = make.getId().trim();
            String normalizedName = make.getName().trim();
            String normalizedKey = normalizedName.toLowerCase();

            if (seenIds.contains(normalizedId) || makeRepository.existsById(normalizedId)) {
                skipped++;
                duplicateIds++;
                continue;
            }
            if (seenNames.contains(normalizedKey) || makeRepository.existsByNameIgnoreCase(normalizedName)) {
                skipped++;
                duplicateNames++;
                continue;
            }

            Make newMake = new Make();
            newMake.setId(normalizedId);
            newMake.setName(normalizedName);
            newMake.setNameAr(make.getNameAr());
            makesToSave.add(newMake);
            seenIds.add(normalizedId);
            seenNames.add(normalizedKey);
        }

        List<Make> savedMakes = makesToSave.isEmpty() ? List.of() : makeRepository.saveAll(makesToSave);
        savedMakes.forEach(saved -> auditLogService.logChange(AuditEntityType.SD_MAKE, saved.getId(),
            AuditAction.CREATE, AuditSource.BULK, actor, null, makeSnapshot(saved), context));

        List<String> reasons = new ArrayList<>(skipReasons);
        if (duplicateNames > 0) {
            reasons.add(String.format("%d record(s) skipped because the make name already exists.", duplicateNames));
        }
        if (invalidNames > 0) {
            reasons.add(String.format("%d record(s) skipped because the make name was missing.", invalidNames));
        }
        if (duplicateIds > 0) {
            reasons.add(String.format("%d record(s) skipped because the make ID already exists.", duplicateIds));
        }
        if (invalidIds > 0) {
            reasons.add(String.format("%d record(s) skipped because the make ID was missing.", invalidIds));
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
    public void deleteMake(String id, User actor, AuditRequestContext context) {
        Make make = makeRepository.findById(id).orElseThrow();
        Map<String, Object> oldSnapshot = makeSnapshot(make);
        List<Model> models = modelRepository.findByMake(make);
        LocalDateTime now = LocalDateTime.now();
        if (!models.isEmpty()) {
            adpMappingRepository.clearModelsFromMappings(models.stream().map(Model::getId).toList(), now);
        }
        adpMappingRepository.clearMakeFromMappings(make.getId(), now);
        modelRepository.deleteAll(models);
        makeRepository.delete(make);
        auditLogService.logChange(AuditEntityType.SD_MAKE, id, AuditAction.DELETE, AuditSource.MANUAL,
            actor, oldSnapshot, null, context);
    }

    private Map<String, Object> makeSnapshot(Make make) {
        if (make == null) {
            return null;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", make.getId());
        snapshot.put("name", make.getName());
        snapshot.put("nameAr", make.getNameAr());
        return snapshot;
    }
}
