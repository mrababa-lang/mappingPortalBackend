package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.service.MakeService;
import com.slashdata.vehicleportal.service.UserLookupService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/makes/bulk")
public class MakeBulkController {

    private final MakeService makeService;
    private final ObjectMapper objectMapper;
    private final UserLookupService userLookupService;

    public MakeBulkController(MakeService makeService, ObjectMapper objectMapper, UserLookupService userLookupService) {
        this.makeService = makeService;
        this.objectMapper = objectMapper;
        this.userLookupService = userLookupService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<BulkUploadResult<Make>>> bulkUpsert(@RequestBody String payload,
                                                                         @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                                                         Principal principal,
                                                                         HttpServletRequest request)
    {
        User actor = userLookupService.findByPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.of(makeService.bulkSave(parsePayload(payload, contentType), actor,
            AuditRequestContext.from(request))));
    }

    private List<Make> parsePayload(String payload, String contentType) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        boolean contentTypeIsCsv = contentType != null && contentType.toLowerCase().contains("text/csv");
        String trimmedPayload = payload.trim();
        boolean looksLikeJson = trimmedPayload.startsWith("[") || trimmedPayload.startsWith("{");

        if (contentTypeIsCsv || !looksLikeJson) {
            return parseCsvPayload(payload);
        }

        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Make.class);

        try {
            return objectMapper.readValue(trimmedPayload, listType);
        } catch (IOException collectionEx) {
            try {
                JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, listType);
                ApiResponse<List<Make>> response = objectMapper.readValue(trimmedPayload, wrapperType);
                if (response.getData() != null) {
                    return response.getData();
                }
            } catch (IOException ignored) {
                // Continue trying other parsing strategies
            }

            try {
                Make make = objectMapper.readValue(trimmedPayload, Make.class);
                return List.of(make);
            } catch (IOException singleObjectEx) {
                return parseCsvPayload(payload);
            }
        }
    }

    private List<Make> parseCsvPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        String[] lines = payload.split("\\r?\\n");
        if (lines.length == 0) {
            return List.of();
        }

        String[] headers = lines[0].split(",");
        int idIndex = findHeaderIndex(headers, "id", "make_id");
        int nameIndex = findHeaderIndex(headers, "name");
        int nameArIndex = findHeaderIndex(headers, "name_ar", "namear", "name_arabic");

        if (nameIndex < 0) {
            return List.of();
        }

        List<Make> makes = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }
            String[] values = lines[i].split(",");
            if (nameIndex >= values.length) {
                continue;
            }
            Make make = new Make();
            if (idIndex >= 0 && idIndex < values.length && !values[idIndex].isBlank()) {
                make.setId(values[idIndex].trim());
            }
            make.setName(values[nameIndex].trim());
            if (nameArIndex >= 0 && nameArIndex < values.length) {
                make.setNameAr(values[nameArIndex].trim());
            }
            makes.add(make);
        }
        return makes;
    }

    private int findHeaderIndex(String[] headers, String... candidates) {
        for (int i = 0; i < headers.length; i++) {
            String header = headers[i].replace("\uFEFF", "").trim().toLowerCase();
            for (String candidate : candidates) {
                if (header.equals(candidate.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }
}
