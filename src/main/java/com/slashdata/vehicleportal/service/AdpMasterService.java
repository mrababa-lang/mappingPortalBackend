package com.slashdata.vehicleportal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.AdpMasterBulkSyncResponse;
import com.slashdata.vehicleportal.dto.AdpMasterBulkUploadResponse;
import com.slashdata.vehicleportal.entity.ADPHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdpMasterService {

    private static final Logger logger = LoggerFactory.getLogger(AdpMasterService.class);

    private static final Map<String, Set<String>> COLUMN_ALIASES = Map.ofEntries(
        Map.entry("adpMakeId", Set.of("adpmakeid", "makecode", "makeid")),
        Map.entry("makeEnDesc", Set.of("makeendesc", "descen", "makeen")),
        Map.entry("makeArDesc", Set.of("makeardesc", "descar", "makear")),
        Map.entry("adpModelId", Set.of("adpmodelid", "modelcode", "modelid")),
        Map.entry("modelEnDesc", Set.of("modelendesc", "modeldescen")),
        Map.entry("modelArDesc", Set.of("modelardesc", "modeldescar")),
        Map.entry("adpTypeId", Set.of("adptypeid", "typecode", "typeid")),
        Map.entry("typeEnDesc", Set.of("typeendesc", "typedescen")),
        Map.entry("typeArDesc", Set.of("typeardesc", "typedescar")),
        Map.entry("kindCode", Set.of("kindcode", "classificationcode")),
        Map.entry("kindEnDesc", Set.of("kindendesc", "classificationen")),
        Map.entry("kindArDesc", Set.of("kindardesc", "kinddescar", "classificationar"))
    );

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

    public AdpMasterBulkUploadResponse bulkUpload(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return new AdpMasterBulkUploadResponse(0, 0, 0, "No records provided.");
        }

        int added = 0;
        int skipped = 0;
        int errorCount = 0;
        int rowNumber = 0;

        for (Map<String, Object> row : rows) {
            rowNumber++;
            UploadResult result = processRow(row, rowNumber);
            added += result.added;
            skipped += result.skipped;
            errorCount += result.errorCount;
        }

        int processedRows = added + skipped + errorCount;
        String message = String.format("Processed %d rows. %d rows failed validation.", processedRows, errorCount);
        return new AdpMasterBulkUploadResponse(added, skipped, errorCount, message);
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
            case BULK_UPLOAD -> {
                registerChange(changes, "makeEnDesc", target.getMakeEnDesc(), source.getMakeEnDesc(),
                    value -> target.setMakeEnDesc((String) value));
                registerChange(changes, "makeArDesc", target.getMakeArDesc(), source.getMakeArDesc(),
                    value -> target.setMakeArDesc((String) value));
                registerChange(changes, "modelEnDesc", target.getModelEnDesc(), source.getModelEnDesc(),
                    value -> target.setModelEnDesc((String) value));
                registerChange(changes, "modelArDesc", target.getModelArDesc(), source.getModelArDesc(),
                    value -> target.setModelArDesc((String) value));
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

    private UploadResult processRow(Map<String, Object> row, int rowNumber) {
        try {
            if (row == null || row.isEmpty()) {
                return UploadResult.skipped();
            }

            Map<String, String> normalizedRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String normalizedKey = normalizeKey(entry.getKey());
                if (normalizedKey == null) {
                    continue;
                }
                Object value = entry.getValue();
                normalizedRow.put(normalizedKey, value == null ? null : String.valueOf(value));
            }

            String adpMakeId = normalizeValue(resolveValue(normalizedRow, "adpMakeId"));
            String adpModelId = normalizeValue(resolveValue(normalizedRow, "adpModelId"));
            String adpTypeId = normalizeValue(resolveValue(normalizedRow, "adpTypeId"));

            if (isBlank(adpMakeId) || isBlank(adpModelId) || isBlank(adpTypeId)) {
                return UploadResult.skipped();
            }

            ADPMaster incoming = new ADPMaster();
            incoming.setAdpMakeId(adpMakeId);
            incoming.setAdpModelId(adpModelId);
            incoming.setMakeEnDesc(normalizeValue(resolveValue(normalizedRow, "makeEnDesc")));
            incoming.setMakeArDesc(normalizeValue(resolveValue(normalizedRow, "makeArDesc")));
            incoming.setModelEnDesc(normalizeValue(resolveValue(normalizedRow, "modelEnDesc")));
            incoming.setModelArDesc(normalizeValue(resolveValue(normalizedRow, "modelArDesc")));
            incoming.setAdpTypeId(adpTypeId);
            incoming.setTypeEnDesc(normalizeValue(resolveValue(normalizedRow, "typeEnDesc")));
            incoming.setTypeArDesc(normalizeValue(resolveValue(normalizedRow, "typeArDesc")));
            incoming.setKindCode(normalizeValue(resolveValue(normalizedRow, "kindCode")));
            incoming.setKindEnDesc(normalizeValue(resolveValue(normalizedRow, "kindEnDesc")));
            incoming.setKindArDesc(normalizeValue(resolveValue(normalizedRow, "kindArDesc")));

            Optional<ADPMaster> existingOpt = adpMasterRepository
                .findByAdpMakeIdAndAdpModelIdAndAdpTypeId(adpMakeId, adpModelId, adpTypeId);
            if (existingOpt.isPresent()) {
                ADPMaster existing = existingOpt.get();
                Map<String, Map<String, Object>> changes = applyUpdates(existing, incoming, UpdateMode.BULK_UPLOAD);
                if (!changes.isEmpty()) {
                    adpMasterRepository.save(existing);
                    recordHistory(existing, "BULK_UPLOAD_SYNC", changes);
                }
                return UploadResult.updated();
            }

            ADPMaster saved = adpMasterRepository.save(incoming);
            recordHistory(saved, "BULK_UPLOAD_SYNC", buildCreatedDetails(saved));
            return UploadResult.added();
        } catch (Exception ex) {
            logger.warn("Failed processing ADP bulk upload row {}", rowNumber, ex);
            return UploadResult.error();
        }
    }

    private String resolveValue(Map<String, String> normalizedRow, String canonicalKey) {
        if (normalizedRow == null || normalizedRow.isEmpty()) {
            return null;
        }
        String normalizedCanonical = normalizeKey(canonicalKey);
        if (normalizedCanonical != null && normalizedRow.containsKey(normalizedCanonical)) {
            return normalizedRow.get(normalizedCanonical);
        }
        Set<String> aliases = COLUMN_ALIASES.get(canonicalKey);
        if (aliases == null) {
            return null;
        }
        for (String alias : aliases) {
            if (normalizedRow.containsKey(alias)) {
                return normalizedRow.get(alias);
            }
        }
        return null;
    }

    private String normalizeKey(String header) {
        if (header == null) {
            return null;
        }
        String trimmed = header.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase().replaceAll("[\\s_]+", "");
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
        SYNC_ONLY,
        BULK_UPLOAD
    }

    private static class UploadResult {
        private final int added;
        private final int skipped;
        private final int errorCount;

        private UploadResult(int added, int skipped, int errorCount) {
            this.added = added;
            this.skipped = skipped;
            this.errorCount = errorCount;
        }

        private static UploadResult added() {
            return new UploadResult(1, 0, 0);
        }

        private static UploadResult updated() {
            return new UploadResult(0, 0, 0);
        }

        private static UploadResult skipped() {
            return new UploadResult(0, 1, 0);
        }

        private static UploadResult error() {
            return new UploadResult(0, 0, 1);
        }
    }
}
