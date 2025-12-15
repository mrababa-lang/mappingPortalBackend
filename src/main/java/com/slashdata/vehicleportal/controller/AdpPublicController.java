package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.slashdata.vehicleportal.dto.AdpAttributeDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp")
public class AdpPublicController {

    private final ADPMasterRepository adpMasterRepository;
    private final CsvMapper csvMapper;

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

    public AdpPublicController(ADPMasterRepository adpMasterRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.csvMapper = new CsvMapper();
        this.csvMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @GetMapping("/makes")
    public ApiResponse<List<AdpAttributeDto>> getMakes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.MAKE));
    }

    @GetMapping("/types")
    public ApiResponse<List<AdpAttributeDto>> getTypes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.TYPE));
    }

    @PostMapping(value = "/master/upload", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ADPMaster>>> uploadMasterJson(@RequestBody List<ADPMaster> records) {
        List<ADPMaster> saved = replaceMasters(records);
        return ResponseEntity.ok(ApiResponse.of(saved));
    }

    @PostMapping(value = "/master/upload", consumes = "text/csv")
    public ResponseEntity<ApiResponse<List<ADPMaster>>> uploadMasterCsv(@RequestBody String csvContent) {
        List<ADPMaster> saved = replaceMasters(parseCsvRecords(csvContent));
        return ResponseEntity.ok(ApiResponse.of(saved));
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
        adpMasterRepository.deleteAll();
        return adpMasterRepository.saveAll(records != null ? records : List.of());
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
