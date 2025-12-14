package com.slashdata.vehicleportal.controller;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.ApiResponse;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.service.MakeService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public MakeBulkController(MakeService makeService, ObjectMapper objectMapper) {
        this.makeService = makeService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, "text/csv"})
    public ResponseEntity<ApiResponse<List<Make>>> bulkUpsert(@RequestBody String payload,
                                                             @RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType)
    {
        return ResponseEntity.ok(ApiResponse.of(makeService.bulkSave(parsePayload(payload, contentType))));
    }

    private List<Make> parsePayload(String payload, String contentType) {
        if (contentType != null && contentType.toLowerCase().contains("text/csv")) {
            return parseCsvPayload(payload);
        }
        try {
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, Make.class);
            return objectMapper.readValue(payload, listType);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to parse make payload", ex);
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
            String header = headers[i].trim().toLowerCase();
            for (String candidate : candidates) {
                if (header.equals(candidate.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }
}
