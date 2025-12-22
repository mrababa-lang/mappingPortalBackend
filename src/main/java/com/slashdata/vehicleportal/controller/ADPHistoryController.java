package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpHistoryEntryDto;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.ADPHistory;
import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import com.slashdata.vehicleportal.repository.ADPHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMappingHistoryRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/adp/history")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
public class ADPHistoryController {

    private final ADPHistoryRepository adpHistoryRepository;
    private final ADPMappingHistoryRepository adpMappingHistoryRepository;

    public ADPHistoryController(ADPHistoryRepository adpHistoryRepository,
                                ADPMappingHistoryRepository adpMappingHistoryRepository) {
        this.adpHistoryRepository = adpHistoryRepository;
        this.adpMappingHistoryRepository = adpMappingHistoryRepository;
    }

    @GetMapping("/{adpId}")
    public ApiResponse<List<AdpHistoryEntryDto>> getHistory(@PathVariable String adpId) {
        if (adpId == null || adpId.isBlank() || "undefined".equalsIgnoreCase(adpId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP id is required");
        }

        List<AdpHistoryEntryDto> entries = Stream.concat(
                adpHistoryRepository.findByAdpMaster_IdOrderByCreatedAtDesc(adpId).stream()
                    .map(this::fromMasterHistory),
                adpMappingHistoryRepository.findByAdpMaster_IdOrderByCreatedAtDesc(adpId).stream()
                    .map(this::fromMappingHistory))
            .sorted(Comparator.comparing(AdpHistoryEntryDto::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed())
            .toList();

        return ApiResponse.of(entries);
    }

    private AdpHistoryEntryDto fromMasterHistory(ADPHistory history) {
        return new AdpHistoryEntryDto(history.getId(), history.getAction(), history.getDetails(),
            history.getCreatedAt(), "MASTER", null, null);
    }

    private AdpHistoryEntryDto fromMappingHistory(ADPMappingHistory history) {
        String userEmail = history.getUser() != null ? history.getUser().getEmail() : null;
        String mappingId = history.getMapping() != null ? history.getMapping().getId() : null;
        return new AdpHistoryEntryDto(history.getId(), history.getAction(), history.getDetails(),
            history.getCreatedAt(), "MAPPING", userEmail, mappingId);
    }
}
