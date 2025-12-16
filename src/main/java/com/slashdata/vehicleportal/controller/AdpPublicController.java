package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.slashdata.vehicleportal.dto.AdpAttributeDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.dto.CreateAdpMakeMappingRequest;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.ADPMakeMapping;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMakeMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
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
    private final ADPMakeMappingRepository adpMakeMappingRepository;
    private final MakeRepository makeRepository;
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
        .setUseHeader(true)
        .setColumnSeparator(',')
        .build();

    public AdpPublicController(ADPMasterRepository adpMasterRepository,
                               ADPMappingRepository adpMappingRepository,
                               ADPMakeMappingRepository adpMakeMappingRepository,
                               MakeRepository makeRepository,
                               ObjectMapper objectMapper) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.adpMakeMappingRepository = adpMakeMappingRepository;
        this.makeRepository = makeRepository;
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

    @PostMapping("/makes/map")
    public ApiResponse<ADPMakeMapping> mapMake(@RequestBody CreateAdpMakeMappingRequest request) {
        if (request == null || request.getAdpMakeId() == null || request.getAdpMakeId().isBlank()
            || request.getSdMakeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "adpMakeId and sdMakeId are required");
        }

        Make sdMake = makeRepository.findById(request.getSdMakeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Make not found"));

        ADPMakeMapping mapping = adpMakeMappingRepository.findByAdpMakeId(request.getAdpMakeId())
            .orElseGet(ADPMakeMapping::new);

        mapping.setAdpMakeId(request.getAdpMakeId());
        mapping.setSdMake(sdMake);
        mapping.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.of(adpMakeMappingRepository.save(mapping));
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
    public ResponseEntity<ApiResponse<BulkUploadResult<ADPMaster>>> uploadMaster(@RequestBody String payload,
                                                                                @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType)
    {
        BulkUploadResult<ADPMaster> result = replaceMasters(parsePayload(payload, contentType));
        return ResponseEntity.ok(ApiResponse.of(result));
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

    private BulkUploadResult<ADPMaster> replaceMasters(List<ADPMaster> records) {
        if (records == null || records.isEmpty()) {
            return new BulkUploadResult<>(List.of(), 0, 0,
                "No ADP master records were uploaded.", List.of("No records were found in the upload payload."));
        }

        adpMappingRepository.deleteAllInBatch();
        adpMasterRepository.deleteAllInBatch();
        List<ADPMaster> saved = adpMasterRepository.saveAll(records);
        String message = String.format("Uploaded %d ADP master record(s).", saved.size());
        return new BulkUploadResult<>(saved, saved.size(), 0, message, List.of());
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
