package com.slashdata.vehicleportal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.AdpMasterBulkSyncResponse;
import com.slashdata.vehicleportal.entity.ADPHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdpMasterService {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPHistoryRepository adpHistoryRepository;
    private final ObjectMapper objectMapper;

    public AdpMasterService(ADPMasterRepository adpMasterRepository,
                            ADPHistoryRepository adpHistoryRepository,
                            ObjectMapper objectMapper) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpHistoryRepository = adpHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public ADPMaster create(ADPMaster request) {
        validateUniqueCombination(request.getAdpMakeId(), request.getAdpModelId(), null);
        ADPMaster saved = adpMasterRepository.save(request);
        recordHistory(saved, "CREATED", buildCreatedDetails(saved));
        return saved;
    }

    public ADPMaster update(String id, ADPMaster request) {
        ADPMaster existing = adpMasterRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP master not found"));
        String effectiveMakeId = request.getAdpMakeId() != null ? request.getAdpMakeId() : existing.getAdpMakeId();
        String effectiveModelId = request.getAdpModelId() != null ? request.getAdpModelId() : existing.getAdpModelId();
        validateUniqueCombination(effectiveMakeId, effectiveModelId, existing.getId());

        Map<String, Map<String, Object>> changes = applyUpdates(existing, request, UpdateMode.FULL);
        ADPMaster saved = adpMasterRepository.save(existing);
        recordHistory(saved, "UPDATED", changes);
        return saved;
    }

    public AdpMasterBulkSyncResponse bulkSync(List<ADPMaster> records) {
        if (records == null || records.isEmpty()) {
            return new AdpMasterBulkSyncResponse(0, 0, "No records provided.");
        }

        int added = 0;
        int skipped = 0;
        int updated = 0;

        for (ADPMaster record : records) {
            if (record == null || isBlank(record.getAdpMakeId()) || isBlank(record.getAdpModelId())) {
                skipped++;
                continue;
            }

            Optional<ADPMaster> existingOpt = adpMasterRepository.findByAdpMakeIdAndAdpModelId(
                record.getAdpMakeId(), record.getAdpModelId());

            if (existingOpt.isPresent()) {
                ADPMaster existing = existingOpt.get();
                Map<String, Map<String, Object>> changes = applyUpdates(existing, record, UpdateMode.SYNC_ONLY);
                if (changes.isEmpty()) {
                    recordHistory(existing, "SYNCED", Map.of("message", "No changes"));
                    skipped++;
                    continue;
                }
                adpMasterRepository.save(existing);
                recordHistory(existing, "SYNCED", changes);
                updated++;
            } else {
                ADPMaster saved = adpMasterRepository.save(record);
                recordHistory(saved, "SYNCED", buildCreatedDetails(saved));
                added++;
            }
        }

        String message = String.format("Sync completed: %d added, %d updated, %d skipped.", added, updated, skipped);
        return new AdpMasterBulkSyncResponse(added, skipped, message);
    }

    private void validateUniqueCombination(String adpMakeId, String adpModelId, String excludeId) {
        if (isBlank(adpMakeId) || isBlank(adpModelId)) {
            return;
        }
        Optional<ADPMaster> existing = adpMasterRepository.findByAdpMakeIdAndAdpModelId(adpMakeId, adpModelId);
        if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ADP make/model combination already exists");
        }
    }

    private Map<String, Object> buildCreatedDetails(ADPMaster master) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("adpMakeId", master.getAdpMakeId());
        details.put("makeEnDesc", master.getMakeEnDesc());
        details.put("makeArDesc", master.getMakeArDesc());
        details.put("adpModelId", master.getAdpModelId());
        details.put("modelEnDesc", master.getModelEnDesc());
        details.put("modelArDesc", master.getModelArDesc());
        details.put("adpTypeId", master.getAdpTypeId());
        details.put("typeEnDesc", master.getTypeEnDesc());
        details.put("typeArDesc", master.getTypeArDesc());
        details.put("kindCode", master.getKindCode());
        details.put("kindEnDesc", master.getKindEnDesc());
        details.put("kindArDesc", master.getKindArDesc());
        return details;
    }

    private Map<String, Map<String, Object>> applyUpdates(ADPMaster target, ADPMaster source, UpdateMode mode) {
        Map<String, Map<String, Object>> changes = new LinkedHashMap<>();
        switch (mode) {
            case FULL -> {
                registerChange(changes, "adpMakeId", target.getAdpMakeId(), source.getAdpMakeId(),
                    value -> target.setAdpMakeId((String) value));
                registerChange(changes, "makeEnDesc", target.getMakeEnDesc(), source.getMakeEnDesc(),
                    value -> target.setMakeEnDesc((String) value));
                registerChange(changes, "makeArDesc", target.getMakeArDesc(), source.getMakeArDesc(),
                    value -> target.setMakeArDesc((String) value));
                registerChange(changes, "adpModelId", target.getAdpModelId(), source.getAdpModelId(),
                    value -> target.setAdpModelId((String) value));
                registerChange(changes, "modelEnDesc", target.getModelEnDesc(), source.getModelEnDesc(),
                    value -> target.setModelEnDesc((String) value));
                registerChange(changes, "modelArDesc", target.getModelArDesc(), source.getModelArDesc(),
                    value -> target.setModelArDesc((String) value));
                registerChange(changes, "adpTypeId", target.getAdpTypeId(), source.getAdpTypeId(),
                    value -> target.setAdpTypeId((String) value));
                registerChange(changes, "typeEnDesc", target.getTypeEnDesc(), source.getTypeEnDesc(),
                    value -> target.setTypeEnDesc((String) value));
                registerChange(changes, "typeArDesc", target.getTypeArDesc(), source.getTypeArDesc(),
                    value -> target.setTypeArDesc((String) value));
                registerChange(changes, "kindCode", target.getKindCode(), source.getKindCode(),
                    value -> target.setKindCode((String) value));
                registerChange(changes, "kindEnDesc", target.getKindEnDesc(), source.getKindEnDesc(),
                    value -> target.setKindEnDesc((String) value));
                registerChange(changes, "kindArDesc", target.getKindArDesc(), source.getKindArDesc(),
                    value -> target.setKindArDesc((String) value));
            }
            case SYNC_ONLY -> {
                registerChange(changes, "makeEnDesc", target.getMakeEnDesc(), source.getMakeEnDesc(),
                    value -> target.setMakeEnDesc((String) value));
                registerChange(changes, "makeArDesc", target.getMakeArDesc(), source.getMakeArDesc(),
                    value -> target.setMakeArDesc((String) value));
                registerChange(changes, "modelEnDesc", target.getModelEnDesc(), source.getModelEnDesc(),
                    value -> target.setModelEnDesc((String) value));
                registerChange(changes, "modelArDesc", target.getModelArDesc(), source.getModelArDesc(),
                    value -> target.setModelArDesc((String) value));
                registerChange(changes, "adpTypeId", target.getAdpTypeId(), source.getAdpTypeId(),
                    value -> target.setAdpTypeId((String) value));
                registerChange(changes, "typeEnDesc", target.getTypeEnDesc(), source.getTypeEnDesc(),
                    value -> target.setTypeEnDesc((String) value));
                registerChange(changes, "typeArDesc", target.getTypeArDesc(), source.getTypeArDesc(),
                    value -> target.setTypeArDesc((String) value));
                registerChange(changes, "kindCode", target.getKindCode(), source.getKindCode(),
                    value -> target.setKindCode((String) value));
                registerChange(changes, "kindEnDesc", target.getKindEnDesc(), source.getKindEnDesc(),
                    value -> target.setKindEnDesc((String) value));
                registerChange(changes, "kindArDesc", target.getKindArDesc(), source.getKindArDesc(),
                    value -> target.setKindArDesc((String) value));
            }
        }
        return changes;
    }

    private void registerChange(Map<String, Map<String, Object>> changes,
                                String field,
                                String before,
                                String after,
                                java.util.function.Consumer<Object> setter) {
        if (after == null || Objects.equals(before, after)) {
            return;
        }
        setter.accept(after);
        Map<String, Object> diff = new LinkedHashMap<>();
        diff.put("from", before);
        diff.put("to", after);
        changes.put(field, diff);
    }

    private void recordHistory(ADPMaster master, String action, Object details) {
        ADPHistory history = new ADPHistory();
        history.setAdpMaster(master);
        history.setAction(action);
        history.setDetails(serializeDetails(details));
        adpHistoryRepository.save(history);
    }

    private String serializeDetails(Object details) {
        if (details == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            return String.valueOf(details);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private enum UpdateMode {
        FULL,
        SYNC_ONLY
    }
}
