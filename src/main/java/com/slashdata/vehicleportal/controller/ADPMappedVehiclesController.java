package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.AdpMappingViewDto;
import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.MappedVehicleExportRow;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/adp/mapped-vehicles")
@PreAuthorize("isAuthenticated()")
public class ADPMappedVehiclesController {

    private static final List<MappingStatus> MAPPED_STATUSES = List.of(MappingStatus.MAPPED);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ADPMappingRepository adpMappingRepository;

    public ADPMappedVehiclesController(ADPMappingRepository adpMappingRepository) {
        this.adpMappingRepository = adpMappingRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    public PagedResponse<AdpMappingViewDto> list(@RequestParam(value = "q", required = false) String query,
                                                 @RequestParam(value = "dateFrom", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                 @RequestParam(value = "dateTo", required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                 @RequestParam(value = "reviewedOnly", defaultValue = "false")
                                                 boolean reviewedOnly,
                                                 @PageableDefault(page = 0, size = 20,
                                                     sort = "updatedAt", direction = Sort.Direction.DESC)
                                                 Pageable pageable) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        Page<AdpMappingViewDto> page = adpMappingRepository.findMappedVehicleViews(query, from, to, reviewedOnly,
            MAPPED_STATUSES, pageable);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportCsv(
        @RequestParam(value = "dateFrom", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
        @RequestParam(value = "dateTo", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
        @RequestParam(value = "reviewedOnly", defaultValue = "false") boolean reviewedOnly,
        @RequestParam(value = "format", defaultValue = "csv") String format) {

        if (!"csv".equalsIgnoreCase(format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported export format");
        }

        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.plusDays(1).atStartOfDay().minusNanos(1) : null;

        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                 Stream<MappedVehicleExportRow> rows = adpMappingRepository.streamMappedVehiclesForExport(MAPPED_STATUSES,
                     from, to, reviewedOnly)) {
                writeCsvHeader(writer);
                rows.forEach(row -> writeCsvRow(writer, row));
                writer.flush();
            }
        };

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mapped_vehicles_export.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(responseBody);
    }

    private void writeCsvHeader(PrintWriter writer) {
        writer.println(String.join(",",
            "ADP ID",
            "ADP Make Description",
            "ADP Model Description",
            "ADP Type Description",
            "SlashData Make Name",
            "SlashData Model Name",
            "SlashData Type Name",
            "Mapping Status",
            "Mapped By",
            "Mapped Date"));
    }

    private void writeCsvRow(PrintWriter writer, MappedVehicleExportRow row) {
        writer.println(String.join(",",
            escape(row.getAdpId()),
            escape(row.getAdpMakeDescription()),
            escape(row.getAdpModelDescription()),
            escape(row.getAdpTypeDescription()),
            escape(row.getSdMakeName()),
            escape(row.getSdModelName()),
            escape(row.getSdTypeName()),
            escape(row.getStatus() != null ? row.getStatus().name() : ""),
            escape(row.getMappedBy()),
            escape(row.getMappedAt() != null ? DATE_TIME_FORMATTER.format(row.getMappedAt()) : "")));
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
