package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpMakeMapRequest;
import com.slashdata.vehicleportal.dto.AdpMakeExportRow;
import com.slashdata.vehicleportal.dto.AdpTypeMapRequest;
import com.slashdata.vehicleportal.dto.AdpTypeExportRow;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.ADPMakeMapping;
import com.slashdata.vehicleportal.entity.ADPTypeMapping;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.VehicleType;
import com.slashdata.vehicleportal.repository.ADPMakeMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.ADPTypeMappingRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.VehicleTypeRepository;
import com.slashdata.vehicleportal.service.AdpMappingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/adp")
@PreAuthorize("isAuthenticated()")
public class ADPUniqueController {

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMakeMappingRepository adpMakeMappingRepository;
    private final ADPTypeMappingRepository adpTypeMappingRepository;
    private final MakeRepository makeRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final AdpMappingService adpMappingService;

    public ADPUniqueController(ADPMasterRepository adpMasterRepository,
                               ADPMakeMappingRepository adpMakeMappingRepository,
                               ADPTypeMappingRepository adpTypeMappingRepository,
                               MakeRepository makeRepository,
                               VehicleTypeRepository vehicleTypeRepository,
                               AdpMappingService adpMappingService) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMakeMappingRepository = adpMakeMappingRepository;
        this.adpTypeMappingRepository = adpTypeMappingRepository;
        this.makeRepository = makeRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.adpMappingService = adpMappingService;
    }

    @GetMapping("/makes")
    public PagedResponse<?> uniqueMakes(@RequestParam(value = "q", required = false) String query,
                                        @RequestParam(value = "status", required = false) String status,
                                        @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return PagedResponse.fromPage(adpMasterRepository.findUniqueMakes(normalizeQuery(query),
            normalizeStatus(status), pageable));
    }

    @PostMapping("/makes/map")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ApiResponse<ADPMakeMapping> mapMake(@Valid @RequestBody AdpMakeMapRequest request,
                                               HttpServletRequest httpRequest) {
        if (request.getAdpMakeId() == null || request.getAdpMakeId().isBlank()
            || request.getSdMakeId() == null || request.getSdMakeId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP make id and SD make id are required");
        }
        Make sdMake = makeRepository.findById(request.getSdMakeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SD make not found"));

        ADPMakeMapping mapping = adpMakeMappingRepository.findTopByAdpMakeIdOrderByUpdatedAtDesc(
            request.getAdpMakeId())
            .orElseGet(ADPMakeMapping::new);
        mapping.setAdpMakeId(request.getAdpMakeId());
        mapping.setSdMake(sdMake);
        mapping.setUpdatedAt(LocalDateTime.now());

        ADPMakeMapping saved = adpMakeMappingRepository.save(mapping);
        adpMappingService.createMissingModelMappingsForMake(request.getAdpMakeId(), sdMake,
            AuditRequestContext.from(httpRequest));

        return ApiResponse.of(saved);
    }

    @GetMapping("/types")
    public PagedResponse<?> uniqueTypes(@RequestParam(value = "q", required = false) String query,
                                        @RequestParam(value = "status", required = false) String status,
                                        @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return PagedResponse.fromPage(adpMasterRepository.findUniqueTypes(normalizeQuery(query),
            normalizeStatus(status), pageable));
    }

    @PostMapping("/types/map")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
    public ApiResponse<ADPTypeMapping> mapType(@Valid @RequestBody AdpTypeMapRequest request) {
        if (request.getAdpTypeId() == null || request.getAdpTypeId().isBlank()
            || request.getSdTypeId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP type id and SD type id are required");
        }
        VehicleType sdType = vehicleTypeRepository.findById(request.getSdTypeId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SD vehicle type not found"));

        ADPTypeMapping mapping = adpTypeMappingRepository.findByAdpTypeId(request.getAdpTypeId())
            .orElseGet(ADPTypeMapping::new);
        mapping.setAdpTypeId(request.getAdpTypeId());
        mapping.setSdType(sdType);
        mapping.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.of(adpTypeMappingRepository.save(mapping));
    }

    @GetMapping("/makes/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportMakes(
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "format", defaultValue = "csv") String format) {

        if (!"csv".equalsIgnoreCase(format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported export format");
        }

        String normalizedStatus = normalizeStatus(status);
        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                 Stream<AdpMakeExportRow> rows = adpMasterRepository.streamUniqueMakesForExport(
                     normalizeQuery(query), normalizedStatus)) {
                writeMakeCsvHeader(writer);
                rows.forEach(row -> writeMakeCsvRow(writer, row));
                writer.flush();
            }
        };

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"adp_makes_export.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(responseBody);
    }

    @GetMapping("/types/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportTypes(
        @RequestParam(value = "q", required = false) String query,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "format", defaultValue = "csv") String format) {

        if (!"csv".equalsIgnoreCase(format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported export format");
        }

        String normalizedStatus = normalizeStatus(status);
        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                 Stream<AdpTypeExportRow> rows = adpMasterRepository.streamUniqueTypesForExport(
                     normalizeQuery(query), normalizedStatus)) {
                writeTypeCsvHeader(writer);
                rows.forEach(row -> writeTypeCsvRow(writer, row));
                writer.flush();
            }
        };

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"adp_types_export.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(responseBody);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return "all";
        }
        if ("mapped".equalsIgnoreCase(status)) {
            return "mapped";
        }
        if ("unmapped".equalsIgnoreCase(status)) {
            return "unmapped";
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeMakeCsvHeader(PrintWriter writer) {
        writer.println(String.join(",",
            "ADP Make ID",
            "English Description",
            "Arabic Description",
            "SlashData Make ID",
            "SlashData Make Name"));
    }

    private void writeMakeCsvRow(PrintWriter writer, AdpMakeExportRow row) {
        writer.println(String.join(",",
            escape(row.getAdpMakeId()),
            escape(row.getAdpMakeName()),
            escape(row.getAdpMakeNameAr()),
            escape(row.getSdMakeId()),
            escape(row.getSdMakeName())));
    }

    private void writeTypeCsvHeader(PrintWriter writer) {
        writer.println(String.join(",",
            "ADP Type ID",
            "English Description",
            "Arabic Description",
            "SlashData Type ID",
            "SlashData Type Name"));
    }

    private void writeTypeCsvRow(PrintWriter writer, AdpTypeExportRow row) {
        writer.println(String.join(",",
            escape(row.getAdpTypeId()),
            escape(row.getAdpTypeName()),
            escape(row.getAdpTypeNameAr()),
            escape(row.getSdTypeId()),
            escape(row.getSdTypeName())));
    }

    private String escape(Object value) {
        if (value == null) {
            return "";
        }
        String stringValue = value.toString();
        String escaped = stringValue.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
