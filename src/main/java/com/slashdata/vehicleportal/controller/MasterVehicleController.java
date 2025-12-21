package com.slashdata.vehicleportal.controller;

import com.slashdata.vehicleportal.dto.PagedResponse;
import com.slashdata.vehicleportal.dto.MasterVehicleExportRow;
import com.slashdata.vehicleportal.dto.MasterVehicleView;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/master/vehicles")
@PreAuthorize("isAuthenticated()")
public class MasterVehicleController {

    private final ModelRepository modelRepository;

    public MasterVehicleController(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    public PagedResponse<MasterVehicleView> list(@RequestParam(value = "q", required = false) String query,
                                                 @RequestParam(value = "makeId", required = false) String makeId,
                                                 @RequestParam(value = "typeId", required = false) Long typeId,
                                                 @RequestParam(value = "kindCode", required = false) String kindCode,
                                                 @PageableDefault(page = 0, size = 20) Pageable pageable) {
        Page<MasterVehicleView> page = modelRepository.findMasterVehicleViews(normalizeQuery(query), makeId,
            normalizeTypeId(typeId), normalizeQuery(kindCode), pageable);
        return PagedResponse.fromPage(page);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_USER', 'MAPPING_ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> exportCsv(
        @RequestParam(value = "makeId", required = false) String makeId,
        @RequestParam(value = "typeId", required = false) Long typeId,
        @RequestParam(value = "kindCode", required = false) String kindCode,
        @RequestParam(value = "format", defaultValue = "csv") String format) {

        if (!"csv".equalsIgnoreCase(format)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported export format");
        }

        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                 Stream<MasterVehicleExportRow> rows = modelRepository.streamMasterVehiclesForExport(makeId,
                     normalizeTypeId(typeId), normalizeQuery(kindCode))) {
                writeCsvHeader(writer);
                rows.forEach(row -> writeCsvRow(writer, row));
                writer.flush();
            }
        };

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"slashdata_master_export.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(responseBody);
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long normalizeTypeId(Long typeId) {
        return typeId;
    }

    private void writeCsvHeader(PrintWriter writer) {
        writer.println(String.join(",",
            "Make ID",
            "Make Name (En)",
            "Make Name (Ar)",
            "Model ID",
            "Model Name (En)",
            "Model Name (Ar)",
            "Type ID",
            "Type Name",
            "Kind Code",
            "Kind Name (En)",
            "Kind Name (Ar)"));
    }

    private void writeCsvRow(PrintWriter writer, MasterVehicleExportRow row) {
        writer.println(String.join(",",
            escape(row.getMakeId()),
            escape(row.getMakeName()),
            escape(row.getMakeNameAr()),
            escape(row.getModelId()),
            escape(row.getModelName()),
            escape(row.getModelNameAr()),
            escape(row.getTypeId()),
            escape(row.getTypeName()),
            escape(row.getKindCode()),
            escape(row.getKindEnDesc()),
            escape(row.getKindArDesc())));
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
