package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.AdpMappingRequest;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
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
import java.util.Objects;
import java.util.List;
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

    public AdpMappingService(ADPMappingRepository adpMappingRepository, ADPMasterRepository adpMasterRepository,
                             MakeRepository makeRepository, ModelRepository modelRepository,
                             UserRepository userRepository, ADPMappingHistoryRepository historyRepository,
                             DashboardStatsService dashboardStatsService) {
        this.adpMappingRepository = adpMappingRepository;
        this.adpMasterRepository = adpMasterRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.dashboardStatsService = dashboardStatsService;
    }

    @Transactional
    public ADPMapping upsert(String adpId, AdpMappingRequest request, User actor) {
        if (request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }

        ADPMaster master = adpMasterRepository.findById(adpId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP master not found"));

        Make make = resolveMake(request.getMakeId());
        Model model = resolveModel(request.getModelId());

        validateStatusRules(request.getStatus(), make, model);
        if (model != null && make != null && !Objects.equals(model.getMake().getId(), make.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model does not belong to make");
        }

        ADPMapping mapping = adpMappingRepository.findByAdpMasterId(master.getId()).orElse(new ADPMapping());
        boolean isNew = mapping.getId() == null;
        mapping.setAdpMaster(master);

        MappingStatus desiredStatus = request.getStatus();
        Model desiredModel = normalizeModel(desiredStatus, model);
        Make desiredMake = normalizeMake(desiredStatus, make);

        boolean dataChanged = hasMappingChanged(mapping, desiredStatus, desiredMake, desiredModel);

        mapping.setMake(desiredMake);
        mapping.setModel(desiredModel);
        mapping.setStatus(desiredStatus);
        if (dataChanged) {
            mapping.setReviewedAt(null);
            mapping.setReviewedBy(null);
        }
        mapping.setUpdatedAt(LocalDateTime.now());
        if (actor != null) {
            mapping.setUpdatedBy(actor);
        }

        ADPMapping saved = adpMappingRepository.save(mapping);
        persistHistory(saved, actor, isNew ? "CREATED" : "UPDATED");
        dashboardStatsService.recalculateDashboardAsync();
        return saved;
    }

    @Transactional
    public void approve(String mappingId, User reviewer) {
        if (mappingId == null || mappingId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP mapping id is required");
        }
        ADPMapping mapping = adpMappingRepository.findById(mappingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP mapping not found"));
        mapping.setReviewedAt(LocalDateTime.now());
        mapping.setReviewedBy(reviewer);
        adpMappingRepository.save(mapping);
        persistHistory(mapping, reviewer, "REVIEWED");
    }

    @Transactional
    public void reject(String mappingId, User actor) {
        if (mappingId == null || mappingId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ADP mapping id is required");
        }
        ADPMapping mapping = adpMappingRepository.findById(mappingId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ADP mapping not found"));
        persistHistory(mapping, actor, "REJECTED");
        adpMappingRepository.delete(mapping);
    }

    @Transactional
    public void bulkApprove(List<String> ids, User reviewer) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mapping ids are required");
        }
        List<ADPMapping> mappings = adpMappingRepository.findAllById(ids);
        if (mappings.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more ADP mappings were not found");
        }
        LocalDateTime now = LocalDateTime.now();
        for (ADPMapping mapping : mappings) {
            mapping.setReviewedAt(now);
            mapping.setReviewedBy(reviewer);
        }
        adpMappingRepository.saveAll(mappings);
        mappings.forEach(mapping -> persistHistory(mapping, reviewer, "REVIEWED"));
    }

    @Transactional
    public void bulkReject(List<String> ids, User actor) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mapping ids are required");
        }
        List<ADPMapping> mappings = adpMappingRepository.findAllById(ids);
        if (mappings.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more ADP mappings were not found");
        }
        mappings.forEach(mapping -> persistHistory(mapping, actor, "REJECTED"));
        adpMappingRepository.deleteAllById(ids);
    }

    public User findUser(String email) {
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    private Make resolveMake(Long makeId) {
        if (makeId == null) {
            return null;
        }
        return makeRepository.findById(makeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Make not found"));
    }

    private Model resolveModel(String modelId) {
        if (modelId == null) {
            return null;
        }
        return modelRepository.findById(modelId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model not found"));
    }

    private void validateStatusRules(MappingStatus status, Make make, Model model) {
        switch (status) {
            case MAPPED:
                if (make == null || model == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Make and model are required when status is MAPPED");
                }
                break;
            case MISSING_MODEL:
                if (make == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Make is required when status is MISSING_MODEL");
                }
                break;
            case MISSING_MAKE:
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported status");
        }
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
        return String.format("Status=%s, Make=%s, Model=%s", mapping.getStatus(), makeName, modelName);
    }
}
