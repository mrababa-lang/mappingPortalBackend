package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.AiBatchMatchResult;
import com.slashdata.vehicleportal.entity.ADPMapping;
import com.slashdata.vehicleportal.entity.ADPMappingHistory;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.AppConfig;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.MappingStatus;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMappingHistoryRepository;
import com.slashdata.vehicleportal.repository.ADPMappingRepository;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.AppConfigRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiBatchMatchingService {

    private static final double DEFAULT_CONFIDENCE_THRESHOLD = 0.90;

    private final ADPMasterRepository adpMasterRepository;
    private final ADPMappingRepository adpMappingRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final ADPMappingHistoryRepository historyRepository;
    private final DashboardStatsService dashboardStatsService;
    private final AppConfigRepository appConfigRepository;

    public AiBatchMatchingService(ADPMasterRepository adpMasterRepository,
                                  ADPMappingRepository adpMappingRepository,
                                  MakeRepository makeRepository, ModelRepository modelRepository,
                                  ADPMappingHistoryRepository historyRepository,
                                  DashboardStatsService dashboardStatsService,
                                  AppConfigRepository appConfigRepository) {
        this.adpMasterRepository = adpMasterRepository;
        this.adpMappingRepository = adpMappingRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.historyRepository = historyRepository;
        this.dashboardStatsService = dashboardStatsService;
        this.appConfigRepository = appConfigRepository;
    }

    @Async
    @Transactional
    public void runBatchMatchingAsync(User actor) {
        processBatchMatching(actor);
    }

    @Transactional
    public AiBatchMatchResult processBatchMatching(User actor) {
        List<ADPMaster> unmapped = adpMasterRepository.findUnmappedRecords();
        if (unmapped.isEmpty()) {
            return new AiBatchMatchResult(0, 0, 0, "No unmapped records found");
        }

        List<Make> makes = makeRepository.findAll();
        List<Model> models = modelRepository.findAll();
        double threshold = resolveConfidenceThreshold();

        String prompt = buildPrompt(makes, models, unmapped);
        int suggestions = 0;
        int applied = 0;

        Map<String, Make> makeById = makes.stream()
            .collect(Collectors.toMap(Make::getId, make -> make));
        Map<Long, Model> modelById = models.stream()
            .collect(Collectors.toMap(Model::getId, model -> model));
        Map<String, ADPMaster> masterById = unmapped.stream()
            .collect(Collectors.toMap(ADPMaster::getId, master -> master));

        for (List<ADPMaster> batch : partition(unmapped, 20)) {
            List<AiMappingSuggestion> predictions = predictBatch(batch, makes, models);
            for (AiMappingSuggestion suggestion : predictions) {
                if (suggestion.getMakeId() != null) {
                    suggestions++;
                }
                boolean aboveThreshold = applySuggestion(suggestion, masterById, makeById, modelById, actor,
                    threshold);
                if (aboveThreshold) {
                    applied++;
                }
            }
        }

        dashboardStatsService.recalculateDashboardAsync();
        return new AiBatchMatchResult(unmapped.size(), suggestions, applied, prompt);
    }

    private List<List<ADPMaster>> partition(List<ADPMaster> source, int size) {
        List<List<ADPMaster>> partitions = new ArrayList<>();
        for (int i = 0; i < source.size(); i += size) {
            int end = Math.min(source.size(), i + size);
            partitions.add(new ArrayList<>(source.subList(i, end)));
        }
        return partitions;
    }

    private List<AiMappingSuggestion> predictBatch(List<ADPMaster> masters, List<Make> makes,
                                                   List<Model> models) {
        List<AiMappingSuggestion> suggestions = new ArrayList<>();
        Map<String, List<Model>> modelsByMake = models.stream()
            .filter(model -> model.getMake() != null)
            .collect(Collectors.groupingBy(model -> model.getMake().getId()));

        for (ADPMaster master : masters) {
            String description = buildDescription(master);
            Make make = guessMake(description, makes);
            Model model = make != null ? guessModel(description, modelsByMake.get(make.getId())) : null;
            double confidence = calculateConfidence(make, model);
            suggestions.add(new AiMappingSuggestion(master.getId(),
                make != null ? make.getId() : null,
                model != null ? model.getId() : null,
                confidence));
        }
        return suggestions;
    }

    private Make guessMake(String description, List<Make> makes) {
        if (description == null) {
            return null;
        }
        String normalized = description.toLowerCase(Locale.ROOT);
        for (Make make : makes) {
            if (make.getName() != null && normalized.contains(make.getName().toLowerCase(Locale.ROOT))) {
                return make;
            }
        }
        return null;
    }

    private Model guessModel(String description, List<Model> models) {
        if (description == null || models == null) {
            return null;
        }
        String normalized = description.toLowerCase(Locale.ROOT);
        for (Model model : models) {
            if (model.getName() != null && normalized.contains(model.getName().toLowerCase(Locale.ROOT))) {
                return model;
            }
        }
        return null;
    }

    private double calculateConfidence(Make make, Model model) {
        if (make == null) {
            return 0.0;
        }
        if (model != null) {
            return 0.95;
        }
        return 0.65;
    }

    private String buildDescription(ADPMaster master) {
        List<String> parts = new ArrayList<>();
        parts.add(Optional.ofNullable(master.getMakeEnDesc()).orElse(""));
        parts.add(Optional.ofNullable(master.getModelEnDesc()).orElse(""));
        parts.add(Optional.ofNullable(master.getTypeEnDesc()).orElse(""));
        return parts.stream()
            .filter(part -> part != null && !part.isBlank())
            .collect(Collectors.joining(" ")).trim();
    }

    private boolean applySuggestion(AiMappingSuggestion suggestion, Map<String, ADPMaster> masters,
                                    Map<String, Make> makeById, Map<Long, Model> modelById,
                                    User actor, double threshold) {
        if (suggestion.getAdpId() == null || suggestion.getMakeId() == null) {
            return false;
        }
        ADPMaster master = masters.get(suggestion.getAdpId());
        if (master == null) {
            return false;
        }
        Make make = makeById.get(suggestion.getMakeId());
        Model model = suggestion.getModelId() != null ? modelById.get(suggestion.getModelId()) : null;

        ADPMapping mapping = adpMappingRepository.findByAdpMasterId(master.getId()).orElse(new ADPMapping());
        mapping.setAdpMaster(master);
        mapping.setMake(make);
        mapping.setModel(model);
        mapping.setStatus(MappingStatus.MAPPED);
        mapping.setReviewedAt(null);
        mapping.setReviewedBy(null);
        mapping.setUpdatedAt(LocalDateTime.now());
        mapping.setUpdatedBy(actor);
        mapping.setAiConfidence(toPercentage(suggestion.getConfidence()));

        ADPMapping saved = adpMappingRepository.save(mapping);
        persistHistory(saved, actor, "AI_SUGGESTED");

        Double confidence = suggestion.getConfidence();
        return confidence != null && confidence >= threshold;
    }

    private Integer toPercentage(Double confidence) {
        if (confidence == null) {
            return null;
        }
        double normalized = confidence > 1 ? confidence / 100.0 : confidence;
        return (int) Math.round(normalized * 100);
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

    private String buildPrompt(List<Make> makes, List<Model> models, List<ADPMaster> masters) {
        String makeListing = makes.stream()
            .map(make -> String.format("%s:%s", make.getId(), make.getName()))
            .collect(Collectors.joining(", "));
        Map<String, List<Model>> modelsByMake = models.stream()
            .filter(model -> model.getMake() != null)
            .collect(Collectors.groupingBy(model -> model.getMake().getId()));
        StringBuilder modelListing = new StringBuilder();
        for (Map.Entry<String, List<Model>> entry : modelsByMake.entrySet()) {
            String modelsForMake = entry.getValue().stream()
                .map(model -> String.format("%s:%s", model.getId(), model.getName()))
                .collect(Collectors.joining(", "));
            modelListing.append(entry.getKey()).append(" -> [").append(modelsForMake).append("]\n");
        }
        String sample = masters.isEmpty() ? "" : buildDescription(masters.get(0));

        return "Context: You are a vehicle data expert.\n"
            + "Internal Makes: [" + makeListing + "]\n"
            + "Internal Models for selected Makes: [" + modelListing + "]\n"
            + "Task: Given the raw description '" + sample
            + "', identify the most likely internal Make ID and Model ID.\n"
            + "Output Format: JSON only { 'makeId': 'TOY', 'modelId': '200', 'confidence': 0.95 }";
    }

    private double resolveConfidenceThreshold() {
        return appConfigRepository.findTopByOrderByIdAsc()
            .map(AppConfig::getAiConfidenceThreshold)
            .filter(Objects::nonNull)
            .filter(value -> value > 0)
            .orElse(DEFAULT_CONFIDENCE_THRESHOLD);
    }

    private static class AiMappingSuggestion {
        private final String adpId;
        private final String makeId;
        private final Long modelId;
        private final Double confidence;

        AiMappingSuggestion(String adpId, String makeId, Long modelId, Double confidence) {
            this.adpId = adpId;
            this.makeId = makeId;
            this.modelId = modelId;
            this.confidence = confidence;
        }

        public String getAdpId() {
            return adpId;
        }

        public String getMakeId() {
            return makeId;
        }

        public Long getModelId() {
            return modelId;
        }

        public Double getConfidence() {
            return confidence;
        }
    }
}
