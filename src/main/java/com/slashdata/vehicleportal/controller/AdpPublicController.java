package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpAttributeDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adp")
public class AdpPublicController {

    private final ADPMasterRepository adpMasterRepository;

    public AdpPublicController(ADPMasterRepository adpMasterRepository) {
        this.adpMasterRepository = adpMasterRepository;
    }

    @GetMapping("/makes")
    public ApiResponse<List<AdpAttributeDto>> getMakes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.MAKE));
    }

    @GetMapping("/types")
    public ApiResponse<List<AdpAttributeDto>> getTypes() {
        return ApiResponse.of(extractDistinctAttributes(AttributeSelector.TYPE));
    }

    @PostMapping(value = "/master/upload", consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<List<ADPMaster>>> uploadMaster(@RequestBody List<ADPMaster> records) {
        adpMasterRepository.deleteAll();
        List<ADPMaster> saved = adpMasterRepository.saveAll(records != null ? records : List.of());
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
}
