package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.dto.BulkUploadResult;
import com.slashdata.vehicleportal.dto.ModelRequest;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.service.ModelService;
import com.slashdata.vehicleportal.service.UserLookupService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.security.Principal;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models/bulk")
@PreAuthorize("hasAnyRole('ADMIN', 'MAPPING_ADMIN')")
public class ModelBulkController {

    private final ModelService modelService;
    private final ObjectMapper objectMapper;
    private final UserLookupService userLookupService;

    public ModelBulkController(ModelService modelService, ObjectMapper objectMapper,
                               UserLookupService userLookupService) {
        this.modelService = modelService;
        this.objectMapper = objectMapper;
        this.userLookupService = userLookupService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<BulkUploadResult<Model>>> bulkCreate(@RequestBody String payload,
                                                                           @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                                                           Principal principal,
                                                                           HttpServletRequest request)
    {
        User actor = userLookupService.findByPrincipal(principal);
        return ResponseEntity.ok(ApiResponse.of(modelService.bulkCreate(parsePayload(payload, contentType), actor,
            AuditRequestContext.from(request))));
    }

    private List<ModelRequest> parsePayload(String payload, String contentType) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        boolean contentTypeIsCsv = contentType != null && contentType.toLowerCase().contains("text/csv");
        String trimmedPayload = payload.trim();
        boolean looksLikeJson = trimmedPayload.startsWith("[") || trimmedPayload.startsWith("{");

        if (contentTypeIsCsv || !looksLikeJson) {
            return parseCsvPayload(payload);
        }

        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, ModelRequest.class);

        try {
            return objectMapper.readValue(trimmedPayload, listType);
        } catch (IOException collectionEx) {
            try {
                JavaType wrapperType = objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, listType);
                ApiResponse<List<ModelRequest>> response = objectMapper.readValue(trimmedPayload, wrapperType);
                if (response.getData() != null) {
                    return response.getData();
                }
            } catch (IOException ignored) {
                // Continue trying other parsing strategies
            }

            try {
                ModelRequest request = objectMapper.readValue(trimmedPayload, ModelRequest.class);
                return List.of(request);
            } catch (IOException singleObjectEx) {
                return parseCsvPayload(payload);
            }
        }
    }

    private List<ModelRequest> parseCsvPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return List.of();
        }

        String[] lines = payload.split("\\r?\\n");
        if (lines.length == 0) {
            return List.of();
        }

        String[] headers = lines[0].split(",");
        int idIndex = findHeaderIndex(headers, "id");
        int makeIdIndex = findHeaderIndex(headers, "makeId", "make_id");
        int typeIdIndex = findHeaderIndex(headers, "typeId", "type_id");
        int nameIndex = findHeaderIndex(headers, "name");
        int nameArIndex = findHeaderIndex(headers, "nameAr", "name_ar", "name_arabic");

        if (idIndex < 0 || makeIdIndex < 0 || typeIdIndex < 0 || nameIndex < 0) {
            return List.of(new ModelRequest());
        }

        List<ModelRequest> requests = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) {
                continue;
            }

            String[] values = lines[i].split(",");
            if (values.length <= Math.max(idIndex, Math.max(makeIdIndex, Math.max(typeIdIndex, nameIndex)))) {
                requests.add(new ModelRequest());
                continue;
            }

            ModelRequest request = new ModelRequest();
            try {
                request.setId(Long.parseLong(values[idIndex].trim()));
            } catch (NumberFormatException ex) {
                requests.add(request);
                continue;
            }
            request.setMakeId(values[makeIdIndex].trim());
            try {
                request.setTypeId(Long.parseLong(values[typeIdIndex].trim()));
            } catch (NumberFormatException ex) {
                requests.add(request);
                continue;
            }
            request.setName(values[nameIndex].trim());
            if (nameArIndex >= 0 && nameArIndex < values.length) {
                request.setNameAr(values[nameArIndex].trim());
            }
            requests.add(request);
        }
        return requests;
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
