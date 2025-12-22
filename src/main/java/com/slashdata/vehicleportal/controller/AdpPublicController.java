package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.slashdata.vehicleportal.dto.AdpAttributeDto;
import com.slashdata.vehicleportal.dto.AdpMasterBulkUploadResponse;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.service.AdpMasterService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp")
public class AdpPublicController {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final AdpMasterService adpMasterService;
    private final CsvMapper csvMapper;
    private final ObjectMapper objectMapper;

    private static final CsvSchema ADP_MASTER_CSV_SCHEMA = CsvSchema.builder()
        .addColumn("adpMakeId")
        .addColumn("makeEnDesc")
        .addColumn("makeArDesc")
        .addColumn("adpModelId")
        .addColumn("modelEnDesc")
        .addColumn("modelArDesc")
        .addColumn("adpTypeId")
        .addColumn("typeEnDesc")
        .addColumn("typeArDesc")
        .addColumn("kindCode")
        .addColumn("kindEnDesc")
        .addColumn("kindArDesc")
        .setUseHeader(true)
        .setColumnSeparator(',')
        .build();

    public AdpPublicController(ADPMasterRepository adpMasterRepository,
                               ADPMappingRepository adpMappingRepository,
                               AdpMasterService adpMasterService,
                               ObjectMapper objectMapper) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.adpMasterService = adpMasterService;
        this.objectMapper = objectMapper;
        this.csvMapper = new CsvMapper();
        this.csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping("/makes/distinct")
    public ApiResponse<List<AdpAttributeDto>> getMakes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.MAKE));
    }

    @GetMapping("/makes/map")
    public ApiResponse<Map<String, AdpAttributeDto>> getMakesMap() {
        return ApiResponse.of(extractDistinctAttributeMap(AttributeSelector.MAKE));
    }

    @GetMapping("/types/distinct")
    public ApiResponse<List<AdpAttributeDto>> getTypes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.TYPE));
    }

    @GetMapping("/types/map")
    public ApiResponse<Map<String, AdpAttributeDto>> getTypesMap() {
        return ApiResponse.of(extractDistinctAttributeMap(AttributeSelector.TYPE));
    }

    @PostMapping(value = "/master/upload", consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<List<ADPMaster>>> uploadMaster(@RequestBody String payload,
                                                                     @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType)
    {
        List<ADPMaster> records = replaceMasters(parsePayload(payload, contentType));
        return ResponseEntity.ok(ApiResponse.of(records));
    }

    @PostMapping(value = "/master/bulk-upload", consumes = "application/json")
    public ApiResponse<AdpMasterBulkUploadResponse> bulkUpload(@RequestBody List<Map<String, Object>> payload,
                                                               HttpServletRequest httpRequest) {
        return ApiResponse.of(adpMasterService.bulkUpload(payload, null, AuditRequestContext.from(httpRequest)));
    }

    private List<AdpAttributeDto> extractDistinctAttributes(AttributeSelector selector) {
        Map<String, AdpAttributeDto> unique = new LinkedHashMap<>();
        for (ADPMaster master : adpMasterRepository.findAll()) {
            String key = selector.idExtractor.apply(master);
            if (key == null || key.isBlank() || unique.containsKey(key)) {
                continue;
            }
            unique.put(key, new AdpAttributeDto(key, selector.enExtractor.apply(master), selector.arExtractor.apply(master)));
        }
        return new ArrayList<>(unique.values());
    }

    private Map<String, AdpAttributeDto> extractDistinctAttributeMap(AttributeSelector selector) {
        Map<String, AdpAttributeDto> unique = new LinkedHashMap<>();
        for (AdpAttributeDto attribute : extractDistinctAttributes(selector)) {
            unique.put(attribute.getId(), attribute);
        }
        return unique;
    }

    private enum AttributeSelector {
        MAKE(ADPMaster::getAdpMakeId, ADPMaster::getMakeEnDesc, ADPMaster::getMakeArDesc),
        TYPE(ADPMaster::getAdpTypeId, ADPMaster::getTypeEnDesc, ADPMaster::getTypeArDesc);

        private final java.util.function.Function<ADPMaster, String> idExtractor;
        private final java.util.function.Function<ADPMaster, String> enExtractor;
        private final java.util.function.Function<ADPMaster, String> arExtractor;

        AttributeSelector(java.util.function.Function<ADPMaster, String> idExtractor,
                          java.util.function.Function<ADPMaster, String> enExtractor,
                          java.util.function.Function<ADPMaster, String> arExtractor) {
            this.idExtractor = idExtractor;
            this.enExtractor = enExtractor;
            this.arExtractor = arExtractor;
        }
    }

    private List<ADPMaster> replaceMasters(List<ADPMaster> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        adpMappingRepository.deleteAllInBatch();
        adpMasterRepository.deleteAllInBatch();
        return adpMasterRepository.saveAll(records);
    }

    private List<ADPMaster> parsePayload(String payload, String contentType) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        boolean contentTypeIsCsv = contentType != null && contentType.toLowerCase().contains("text/csv");
        String trimmedPayload = payload.trim();
        boolean looksLikeJson = trimmedPayload.startsWith("[") || trimmedPayload.startsWith("{");

        if (contentTypeIsCsv || !looksLikeJson) {
            return parseCsvRecords(payload);
        }

        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ADPMaster.class);

        try {
            return objectMapper.readValue(trimmedPayload, listType);
        } catch (IOException collectionEx) {
            try {
                JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, listType);
                ApiResponse<List<ADPMaster>> response = objectMapper.readValue(trimmedPayload, wrapperType);
                if (response.getData() != null) {
                    return response.getData();
                }
            } catch (IOException ignored) {
                // Continue trying other parsing strategies
            }

            try {
                ADPMaster record = objectMapper.readValue(trimmedPayload, ADPMaster.class);
                return List.of(record);
            } catch (IOException singleObjectEx) {
                return parseCsvRecords(payload);
            }
        }
    }

    private List<ADPMaster> parseCsvRecords(String csvContent) {
        if (csvContent == null || csvContent.isBlank()) {
            return List.of();
        }
        try (MappingIterator<ADPMaster> iterator = csvMapper
            .readerFor(ADPMaster.class)
            .with(ADP_MASTER_CSV_SCHEMA)
            .readValues(csvContent)) {
            return iterator.readAll();
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV payload", exception);
        }
    }
}
