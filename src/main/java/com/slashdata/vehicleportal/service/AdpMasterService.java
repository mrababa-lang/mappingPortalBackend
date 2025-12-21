package com.slashdata.vehicleportal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.slashdata.vehicleportal.dto.AdpMasterBulkSyncResponse;
import com.slashdata.vehicleportal.dto.AdpMasterBulkUploadResponse;
import com.slashdata.vehicleportal.entity.ADPHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdpMasterService {

    private static final Logger logger = LoggerFactory.getLogger(AdpMasterService.class);

    private static final List<String> DEFAULT_COLUMNS = List.of(
        "adpMakeId",
        "makeEnDesc",
        "makeArDesc",
        "adpModelId",
        "modelEnDesc",
        "modelArDesc",
        "adpTypeId",
        "typeEnDesc",
        "typeArDesc",
        "kindCode",
        "kindEnDesc",
        "kindArDesc"
    );

    private static final Map<String, Set<String>> COLUMN_ALIASES = Map.ofEntries(
        Map.entry("adpMakeId", Set.of("make code", "make_id", "makeid", "adpmakeid")),
        Map.entry("makeEnDesc", Set.of("desc en", "make_en", "makedescription", "makeendesc")),
        Map.entry("makeArDesc", Set.of("desc ar", "make_ar", "makeardesc")),
        Map.entry("adpModelId", Set.of("model code", "model_id", "adpmodelid")),
        Map.entry("modelEnDesc", Set.of("model desc en", "modelendesc")),
        Map.entry("modelArDesc", Set.of("model desc ar", "modelardesc")),
        Map.entry("adpTypeId", Set.of("type code", "type_id", "adptypeid")),
        Map.entry("typeEnDesc", Set.of("type desc en", "typeendesc")),
        Map.entry("typeArDesc", Set.of("type desc ar", "typeardesc")),
        Map.entry("kindCode", Set.of("kind code", "kindcode")),
        Map.entry("kindEnDesc", Set.of("kind desc en", "kindendesc")),
        Map.entry("kindArDesc", Set.of("kind desc ar", "kindardesc"))
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

    public AdpMasterBulkUploadResponse bulkUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided for upload");
        }

        int added = 0;
        int skipped = 0;
        int errorCount = 0;
        int rowNumber = 0;

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        CsvSchema schema = CsvSchema.emptySchema().withColumnSeparator(',');

        try (InputStream inputStream = file.getInputStream();
             MappingIterator<String[]> iterator = csvMapper.readerFor(String[].class).with(schema).readValues(inputStream)) {

            if (!iterator.hasNext()) {
                return new AdpMasterBulkUploadResponse(0, 0, 0, "No records provided.");
            }

            String[] firstRow = iterator.next();
            rowNumber++;
            ColumnMapping mapping = resolveColumnMapping(firstRow);

            if (!mapping.hasHeader) {
                UploadResult result = processRow(firstRow, rowNumber, mapping);
                added += result.added;
                skipped += result.skipped;
                errorCount += result.errorCount;
            }

            while (iterator.hasNext()) {
                String[] row = iterator.next();
                rowNumber++;
                UploadResult result = processRow(row, rowNumber, mapping);
                added += result.added;
                skipped += result.skipped;
                errorCount += result.errorCount;
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV file", exception);
        }

        String message = String.format("Synchronization complete. %d rows failed due to format issues.", errorCount);
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

    private ColumnMapping resolveColumnMapping(String[] firstRow) {
        Map<String, Integer> indexes = new LinkedHashMap<>();
        boolean hasHeaderMatch = false;

        for (String column : DEFAULT_COLUMNS) {
            indexes.put(column, -1);
        }

        for (int i = 0; i < firstRow.length; i++) {
            String normalized = normalizeHeader(firstRow[i]);
            if (normalized == null) {
                continue;
            }
            for (Map.Entry<String, Set<String>> entry : COLUMN_ALIASES.entrySet()) {
                if (entry.getValue().contains(normalized)) {
                    indexes.put(entry.getKey(), i);
                    hasHeaderMatch = true;
                }
            }
        }

        if (!hasHeaderMatch) {
            for (int i = 0; i < DEFAULT_COLUMNS.size(); i++) {
                indexes.put(DEFAULT_COLUMNS.get(i), i);
            }
        }

        return new ColumnMapping(indexes, hasHeaderMatch);
    }

    private UploadResult processRow(String[] row, int rowNumber, ColumnMapping mapping) {
        try {
            String adpMakeId = normalizeValue(getValue(row, mapping.indexes.get("adpMakeId")));
            String adpModelId = normalizeValue(getValue(row, mapping.indexes.get("adpModelId")));

            if (isBlank(adpMakeId) || isBlank(adpModelId)) {
                return UploadResult.skipped();
            }

            ADPMaster incoming = new ADPMaster();
            incoming.setAdpMakeId(adpMakeId);
            incoming.setAdpModelId(adpModelId);
            incoming.setMakeEnDesc(normalizeValue(getValue(row, mapping.indexes.get("makeEnDesc"))));
            incoming.setMakeArDesc(normalizeValue(getValue(row, mapping.indexes.get("makeArDesc"))));
            incoming.setModelEnDesc(normalizeValue(getValue(row, mapping.indexes.get("modelEnDesc"))));
            incoming.setModelArDesc(normalizeValue(getValue(row, mapping.indexes.get("modelArDesc"))));
            incoming.setAdpTypeId(normalizeValue(getValue(row, mapping.indexes.get("adpTypeId"))));
            incoming.setTypeEnDesc(normalizeValue(getValue(row, mapping.indexes.get("typeEnDesc"))));
            incoming.setTypeArDesc(normalizeValue(getValue(row, mapping.indexes.get("typeArDesc"))));
            incoming.setKindCode(normalizeValue(getValue(row, mapping.indexes.get("kindCode"))));
            incoming.setKindEnDesc(normalizeValue(getValue(row, mapping.indexes.get("kindEnDesc"))));
            incoming.setKindArDesc(normalizeValue(getValue(row, mapping.indexes.get("kindArDesc"))));

            Optional<ADPMaster> existingOpt = adpMasterRepository.findByAdpMakeIdAndAdpModelId(adpMakeId, adpModelId);
            if (existingOpt.isPresent()) {
                ADPMaster existing = existingOpt.get();
                Map<String, Map<String, Object>> changes = applyUpdates(existing, incoming, UpdateMode.SYNC_ONLY);
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

    private String normalizeHeader(String header) {
        if (header == null) {
            return null;
        }
        String trimmed = header.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getValue(String[] row, Integer index) {
        if (index == null || index < 0 || index >= row.length) {
            return null;
        }
        return row[index];
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

    private static class ColumnMapping {
        private final Map<String, Integer> indexes;
        private final boolean hasHeader;

        private ColumnMapping(Map<String, Integer> indexes, boolean hasHeader) {
            this.indexes = indexes;
            this.hasHeader = hasHeader;
        }
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
