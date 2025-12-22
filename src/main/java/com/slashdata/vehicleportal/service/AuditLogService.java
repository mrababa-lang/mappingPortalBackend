package com.slashdata.vehicleportal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slashdata.vehicleportal.dto.AuditLogChangeDto;
import com.slashdata.vehicleportal.dto.AuditLogDto;
import com.slashdata.vehicleportal.dto.AuditPerformanceDto;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.AuditAction;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditLog;
import com.slashdata.vehicleportal.entity.AuditSource;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.AuditLogRepository;
import com.slashdata.vehicleportal.specification.AuditLogSpecifications;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private static final double MANUAL_ACCURACY = 98.0;
    private static final double MANUAL_WEIGHT = 0.5;
    private static final double AI_WEIGHT = 0.5;

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public AuditLog logChange(AuditEntityType entityType,
                              String entityId,
                              AuditAction action,
                              AuditSource source,
                              User user,
                              Object oldValues,
                              Object newValues,
                              AuditRequestContext context) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setSource(source);
        log.setUser(user);
        log.setOldValues(serializeValues(oldValues));
        log.setNewValues(serializeValues(newValues));
        if (context != null) {
            log.setIpAddress(context.ipAddress());
            log.setUserAgent(context.userAgent());
        }
        log.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    public Page<AuditLogDto> searchLogs(Long userId,
                                        AuditSource source,
                                        LocalDateTime from,
                                        LocalDateTime to,
                                        Pageable pageable) {
        Specification<AuditLog> spec = Specification
            .where(AuditLogSpecifications.hasUserId(userId))
            .and(AuditLogSpecifications.hasSource(source))
            .and(AuditLogSpecifications.withinDateRange(from, to));
        return auditLogRepository.findAll(spec, pageable).map(this::toDto);
    }

    public AuditPerformanceDto calculatePerformance(LocalDateTime from, LocalDateTime to) {
        Specification<AuditLog> baseSpec = Specification
            .where(AuditLogSpecifications.hasEntityType(AuditEntityType.MAPPING))
            .and(AuditLogSpecifications.withinDateRange(from, to));

        List<AuditLog> mappingLogs = auditLogRepository.findAll(baseSpec);

        Map<Long, Long> mappingsPerUser = mappingLogs.stream()
            .filter(log -> log.getUser() != null)
            .filter(log -> log.getAction() == AuditAction.CREATE || log.getAction() == AuditAction.UPDATE)
            .collect(Collectors.groupingBy(log -> log.getUser().getId(), Collectors.counting()));

        long totalAiMappings = mappingLogs.stream()
            .filter(log -> log.getSource() == AuditSource.AI)
            .filter(log -> log.getAction() == AuditAction.CREATE || log.getAction() == AuditAction.UPDATE)
            .count();

        List<AuditLog> approvals = mappingLogs.stream()
            .filter(log -> log.getAction() == AuditAction.APPROVE)
            .toList();

        long approvedAiMappings = approvals.stream()
            .filter(approval -> isApprovedAiMapping(approval, mappingLogs))
            .count();

        double accuracyScore = totalAiMappings == 0
            ? 0.0
            : (approvedAiMappings * 100.0) / totalAiMappings;

        double integrityScore = (MANUAL_ACCURACY * MANUAL_WEIGHT) + (accuracyScore * AI_WEIGHT);

        return new AuditPerformanceDto(mappingsPerUser, accuracyScore, integrityScore, totalAiMappings, approvedAiMappings);
    }

    public List<AuditLogDto> getAuditHistory(List<AuditLog> logs) {
        return logs.stream()
            .sorted(Comparator.comparing(AuditLog::getTimestamp))
            .map(this::toDto)
            .toList();
    }

    private boolean isApprovedAiMapping(AuditLog approvalLog, List<AuditLog> mappingLogs) {
        if (approvalLog == null) {
            return false;
        }
        String entityId = approvalLog.getEntityId();
        AuditLog lastChange = mappingLogs.stream()
            .filter(log -> Objects.equals(log.getEntityId(), entityId))
            .filter(log -> log.getTimestamp().isBefore(approvalLog.getTimestamp())
                || log.getTimestamp().isEqual(approvalLog.getTimestamp()))
            .filter(log -> log.getAction() == AuditAction.CREATE || log.getAction() == AuditAction.UPDATE)
            .max(Comparator.comparing(AuditLog::getTimestamp))
            .orElse(null);
        return lastChange != null && lastChange.getSource() == AuditSource.AI;
    }

    private AuditLogDto toDto(AuditLog log) {
        String userFullName = log.getUser() != null ? log.getUser().getFullName() : null;
        Long userId = log.getUser() != null ? log.getUser().getId() : null;
        return new AuditLogDto(
            log.getId(),
            log.getEntityType(),
            log.getEntityId(),
            log.getAction(),
            log.getSource(),
            userId,
            userFullName,
            log.getOldValues(),
            log.getNewValues(),
            diff(log.getOldValues(), log.getNewValues()),
            log.getIpAddress(),
            log.getUserAgent(),
            log.getTimestamp()
        );
    }

    private List<AuditLogChangeDto> diff(String oldJson, String newJson) {
        Map<String, Object> oldMap = parseJsonMap(oldJson);
        Map<String, Object> newMap = parseJsonMap(newJson);
        Set<String> keys = new HashSet<>();
        keys.addAll(oldMap.keySet());
        keys.addAll(newMap.keySet());

        List<AuditLogChangeDto> changes = new ArrayList<>();
        List<String> sortedKeys = keys.stream().sorted().toList();
        for (String key : sortedKeys) {
            Object oldValue = oldMap.get(key);
            Object newValue = newMap.get(key);
            if (!Objects.equals(oldValue, newValue)) {
                changes.add(new AuditLogChangeDto(key, oldValue, newValue));
            }
        }
        return changes;
    }

    private Map<String, Object> parseJsonMap(String payload) {
        if (payload == null || payload.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private String serializeValues(Object values) {
        if (values == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception ex) {
            return String.valueOf(values);
        }
    }
}
