package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.AdpMappingRequest;
import com.slashdata.vehicleportal.dto.AuditRequestContext;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.AuditAction;
import com.slashdata.vehicleportal.entity.AuditEntityType;
import com.slashdata.vehicleportal.entity.AuditSource;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMappingHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import com.slashdata.vehicleportal.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AdpMappingService {

    private final ADPMappingRepository adpMappingRepository;
    private final ADPMasterRepository adpMasterRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final UserRepository userRepository;
    private final ADPMappingHistoryRepository historyRepository;
    private final DashboardStatsService dashboardStatsService;
    private final AuditLogService auditLogService;

    public AdpMappingService(ADPMappingRepository adpMappingRepository, ADPMasterRepository adpMasterRepository,
                             MakeRepository makeRepository, ModelRepository modelRepository,
                             UserRepository userRepository, ADPMappingHistoryRepository historyRepository,
                             DashboardStatsService dashboardStatsService, AuditLogService auditLogService) {
        this.adpMappingRepository = adpMappingRepository;
        this.adpMasterRepository = adpMasterRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.dashboardStatsService = dashboardStatsService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public int createMissingModelMappingsForMake(String adpMakeId, Make sdMake, AuditRequestContext context) {
        if (adpMakeId == null || adpMakeId.isBlank() || sdMake == null) {
            return 0;
        }

        List<ADPMaster> masters = adpMasterRepository.findAllByAdpMakeId(adpMakeId);
        if (masters.isEmpty()) {
            return 0;
        }

        List<String> masterIds = masters.stream()
            .map(ADPMaster::getId)
            .collect(Collectors.toList());
        Set<String> mappedMasterIds = adpMappingRepository.findByAdpMaster_IdIn(masterIds).stream()
            .map(mapping -> mapping.getAdpMaster().getId())
            .collect(Collectors.toSet());

        List<ADPMapping> newMappings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (ADPMaster master : masters) {
            if (mappedMasterIds.contains(master.getId())) {
                continue;
            }
            ADPMapping mapping = new ADPMapping();
            mapping.setAdpMaster(master);
            mapping.setMake(sdMake);
            mapping.setStatus(MappingStatus.MISSING_MODEL);
            mapping.setUpdatedAt(now);
            newMappings.add(mapping);
        }

        if (newMappings.isEmpty()) {
            return 0;
        }

        List<ADPMapping> saved = adpMappingRepository.saveAll(newMappings);
        saved.forEach(mapping -> {
            persistHistory(mapping, null, "CREATED");
            auditLogService.logChange(AuditEntityType.MAPPING, mapping.getId(), AuditAction.CREATE, AuditSource.BULK,
                null, null, mappingSnapshot(mapping), context);
        });
        dashboardStatsService.recalculateDashboardAsync();
        return saved.size();
    }

    @Transactional
    public ADPMapping upsert(String adpId, AdpMappingRequest request, User actor, AuditRequestContext context) {
        if (request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        ADPMaster master = adpMasterRepository.findById(adpId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP master not found"));

        MappingStatus desiredStatus = request.getStatus();
        Make make = resolveMakeForStatus(desiredStatus, request.getMakeId());
        Model model = resolveModelForStatus(desiredStatus, request.getModelId());
        validateBelongsToMake(desiredStatus, make, model);

        ADPMapping mapping = adpMappingRepository.findByAdpMasterId(master.getId()).orElse(new ADPMapping());
        Map<String, Object> oldSnapshot = mapping.getId() == null ? null : mappingSnapshot(mapping);
        boolean isNew = mapping.getId() == null;
        mapping.setAdpMaster(master);

        Model desiredModel = normalizeModel(desiredStatus, model);
        Make desiredMake = normalizeMake(desiredStatus, make);

        boolean dataChanged = hasMappingChanged(mapping, desiredStatus, desiredMake, desiredModel);

        mapping.setMake(desiredMake);
        mapping.setModel(desiredModel);
        mapping.setStatus(desiredStatus);
        mapping.setAiConfidence(request.getConfidence());
        mapping.setMatchingEngine(request.getMatchingEngine());
        mapping.setAutoPropagated(request.getAutoPropagated());

        boolean aiDriven = request.getConfidence() != null
            || request.getMatchingEngine() != null
            || Boolean.TRUE.equals(request.getAutoPropagated());

        if (dataChanged || aiDriven) {
            mapping.setReviewedAt(null);
            mapping.setReviewedBy(null);
        }
        mapping.setUpdatedAt(LocalDateTime.now());
        if (actor != null) {
            mapping.setUpdatedBy(actor);
        }

        ADPMapping saved = adpMappingRepository.save(mapping);
        persistHistory(saved, actor, aiDriven ? "AI_MAPPED" : "MANUAL_UPDATE");
        auditLogService.logChange(AuditEntityType.MAPPING, saved.getId(),
            isNew ? AuditAction.CREATE : AuditAction.UPDATE,
            aiDriven ? AuditSource.AI : AuditSource.MANUAL,
            actor, oldSnapshot, mappingSnapshot(saved), context);
        dashboardStatsService.recalculateDashboardAsync();
        return saved;
    }

    @Transactional
    public void approve(String adpId, User reviewer, AuditRequestContext context) {
        ADPMapping mapping = getMappingOrThrow(adpId);
        Map<String, Object> oldSnapshot = mappingSnapshot(mapping);
        mapping.setReviewedAt(LocalDateTime.now());
        mapping.setReviewedBy(reviewer);
        adpMappingRepository.save(mapping);
        persistHistory(mapping, reviewer, "REVIEWED");
        auditLogService.logChange(AuditEntityType.MAPPING, mapping.getId(), AuditAction.APPROVE, AuditSource.MANUAL,
            reviewer, oldSnapshot, mappingSnapshot(mapping), context);
    }

    @Transactional
    public void reject(String adpId, User actor, AuditRequestContext context) {
        ADPMapping mapping = getMappingOrThrow(adpId);
        Map<String, Object> oldSnapshot = mappingSnapshot(mapping);
        persistHistory(mapping, actor, "REJECTED");
        auditLogService.logChange(AuditEntityType.MAPPING, mapping.getId(), AuditAction.REJECT, AuditSource.MANUAL,
            actor, oldSnapshot, null, context);
        adpMappingRepository.delete(mapping);
    }

    @Transactional
    public void bulkApprove(List<String> ids, User reviewer, AuditRequestContext context) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mapping ids are required");
        }
        List<ADPMapping> mappings = adpMappingRepository.findAllById(ids);
        if (mappings.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more ADP mappings were not found");
        }
        LocalDateTime now = LocalDateTime.now();
        Map<String, Map<String, Object>> snapshots = new LinkedHashMap<>();
        for (ADPMapping mapping : mappings) {
            snapshots.put(mapping.getId(), mappingSnapshot(mapping));
            mapping.setReviewedAt(now);
            mapping.setReviewedBy(reviewer);
        }
        adpMappingRepository.saveAll(mappings);
        mappings.forEach(mapping -> {
            persistHistory(mapping, reviewer, "REVIEWED");
            auditLogService.logChange(AuditEntityType.MAPPING, mapping.getId(), AuditAction.APPROVE, AuditSource.BULK,
                reviewer, snapshots.get(mapping.getId()), mappingSnapshot(mapping), context);
        });
    }

    @Transactional
    public void bulkReject(List<String> ids, User actor, AuditRequestContext context) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mapping ids are required");
        }
        List<ADPMapping> mappings = adpMappingRepository.findAllById(ids);
        if (mappings.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more ADP mappings were not found");
        }
        mappings.forEach(mapping -> {
            persistHistory(mapping, actor, "REJECTED");
            auditLogService.logChange(AuditEntityType.MAPPING, mapping.getId(), AuditAction.REJECT, AuditSource.BULK,
                actor, mappingSnapshot(mapping), null, context);
        });
        adpMappingRepository.deleteAllById(ids);
    }

    public User findUser(String email) {
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private ADPMapping getMappingOrThrow(String adpId) {
        if (adpId == null || adpId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP mapping id is required");
        }
        return adpMappingRepository.findByAdpMasterId(adpId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP mapping not found"));
    }

    private Make resolveMakeForStatus(MappingStatus status, String makeId) {
        switch (status) {
            case MAPPED:
                if (makeId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Make is required when status is MAPPED");
                }
                return resolveMake(makeId);
            case MISSING_MODEL:
                if (makeId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Make is required when status is MISSING_MODEL");
                }
                return resolveMake(makeId);
            case MISSING_MAKE:
                return null;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported status");
        }
    }

    private Model resolveModelForStatus(MappingStatus status, Long modelId) {
        switch (status) {
            case MAPPED:
                if (modelId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Model is required when status is MAPPED");
                }
                return resolveModel(modelId);
            case MISSING_MODEL:
            case MISSING_MAKE:
                return null;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported status");
        }
    }

    private void validateBelongsToMake(MappingStatus status, Make make, Model model) {
        if (status == MappingStatus.MAPPED && model != null && make != null
            && !Objects.equals(model.getMake().getId(), make.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model does not belong to make");
        }
    }

    private Make resolveMake(String makeId) {
        if (makeId == null) {
            return null;
        }
        return makeRepository.findById(makeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Make not found"));
    }

    private Model resolveModel(Long modelId) {
        if (modelId == null) {
            return null;
        }
        return modelRepository.findById(modelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model not found"));
    }

    private Model normalizeModel(MappingStatus status, Model model) {
        if (status == MappingStatus.MISSING_MODEL || status == MappingStatus.MISSING_MAKE) {
            return null;
        }
        return model;
    }

    private Make normalizeMake(MappingStatus status, Make make) {
        if (status == MappingStatus.MISSING_MAKE) {
            return null;
        }
        return make;
    }

    private boolean hasMappingChanged(ADPMapping mapping, MappingStatus status, Make make, Model model) {
        return mapping.getId() == null
            || mapping.getStatus() != status
            || !Objects.equals(mapping.getMake(), make)
            || !Objects.equals(mapping.getModel(), model);
    }

    private void persistHistory(ADPMapping mapping, User actor, String action) {
        ADPMappingHistory history = new ADPMappingHistory();
        history.setAdpMaster(mapping.getAdpMaster());
        history.setMapping(mapping);
        history.setAction(action);
        history.setDetails(buildDetails(mapping));
        history.setUser(actor);
        historyRepository.save(history);
    }

    private String buildDetails(ADPMapping mapping) {
        String makeName = mapping.getMake() != null ? mapping.getMake().getName() : "(none)";
        String modelName = mapping.getModel() != null ? mapping.getModel().getName() : "(none)";
        String engine = mapping.getMatchingEngine() != null ? mapping.getMatchingEngine() : "(not set)";
        String auto = mapping.getAutoPropagated() != null && mapping.getAutoPropagated() ? "auto" : "manual";
        return String.format("Status=%s, Make=%s, Model=%s, Engine=%s, Propagation=%s",
            mapping.getStatus(), makeName, modelName, engine, auto);
    }

    private Map<String, Object> mappingSnapshot(ADPMapping mapping) {
        if (mapping == null) {
            return null;
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", mapping.getId());
        snapshot.put("adpMasterId", mapping.getAdpMaster() != null ? mapping.getAdpMaster().getId() : null);
        snapshot.put("makeId", mapping.getMake() != null ? mapping.getMake().getId() : null);
        snapshot.put("modelId", mapping.getModel() != null ? mapping.getModel().getId() : null);
        snapshot.put("status", mapping.getStatus());
        snapshot.put("aiConfidence", mapping.getAiConfidence());
        snapshot.put("matchingEngine", mapping.getMatchingEngine());
        snapshot.put("autoPropagated", mapping.getAutoPropagated());
        snapshot.put("updatedAt", mapping.getUpdatedAt());
        snapshot.put("updatedBy", mapping.getUpdatedBy() != null ? mapping.getUpdatedBy().getId() : null);
        snapshot.put("reviewedAt", mapping.getReviewedAt());
        snapshot.put("reviewedBy", mapping.getReviewedBy() != null ? mapping.getReviewedBy().getId() : null);
        return snapshot;
    }
}
