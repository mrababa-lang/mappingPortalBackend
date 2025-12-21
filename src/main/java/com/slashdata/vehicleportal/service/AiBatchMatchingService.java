package com.slashdata.vehicleportal.service;

import com.slashdata.vehicleportal.dto.AiBatchMatchRequest;
import com.slashdata.vehicleportal.dto.AiBatchMatchResult;
import com.slashdata.vehicleportal.dto.AiBatchSuggestion;
import com.slashdata.vehicleportal.entity.ADPMaster;
import com.slashdata.vehicleportal.entity.Make;
import com.slashdata.vehicleportal.entity.Model;
import com.slashdata.vehicleportal.entity.User;
import com.slashdata.vehicleportal.repository.ADPMasterRepository;
import com.slashdata.vehicleportal.repository.MakeRepository;
import com.slashdata.vehicleportal.repository.ModelRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiBatchMatchingService {

    private final ADPMasterRepository adpMasterRepository;
    private final MakeRepository makeRepository;
    private final ModelRepository modelRepository;
    private final DashboardStatsService dashboardStatsService;
    private final AppConfigService appConfigService;

    public AiBatchMatchingService(ADPMasterRepository adpMasterRepository,
                                  MakeRepository makeRepository, ModelRepository modelRepository,
                                  DashboardStatsService dashboardStatsService,
                                  AppConfigService appConfigService) {
        this.adpMasterRepository = adpMasterRepository;
        this.makeRepository = makeRepository;
        this.modelRepository = modelRepository;
        this.dashboardStatsService = dashboardStatsService;
        this.appConfigService = appConfigService;
    }

    @Async
    @Transactional
    public void runBatchMatchingAsync(User actor) {
        processBatchMatching(actor, new AiBatchMatchRequest());
    }

    @Transactional
    public AiBatchMatchResult processBatchMatching(User actor, AiBatchMatchRequest request) {
        Pageable pageable = PageRequest.of(Math.max(0, request.getPage()), Math.max(1, request.getSize()));
        Page<ADPMaster> unmappedPage = adpMasterRepository.findUnmappedRecords(pageable);
        if (unmappedPage.isEmpty()) {
            return new AiBatchMatchResult(new ArrayList<>(), 0, "No unmapped records found");
        }

        List<ADPMaster> unmapped = unmappedPage.getContent();
        List<Make> makes = makeRepository.findAll();
        List<Model> models = modelRepository.findAll();
        String prompt = buildPrompt(makes, models);

        List<AiBatchSuggestion> suggestions = predictBatch(unmapped, makes, models).stream()
            .map(suggestion -> new AiBatchSuggestion(
                suggestion.getAdpId(),
                suggestion.getMakeId(),
                suggestion.getModelId(),
                toPercentage(suggestion.getConfidence())
            ))
            .collect(Collectors.toList());

        dashboardStatsService.recalculateDashboardAsync();
        return new AiBatchMatchResult(suggestions, (int) unmappedPage.getTotalElements(), prompt);
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

    private Integer toPercentage(Double confidence) {
        if (confidence == null) {
            return null;
        }
        double normalized = confidence > 1 ? confidence / 100.0 : confidence;
        return (int) Math.round(normalized * 100);
    }

    private String buildPrompt(List<Make> makes, List<Model> models) {
        String systemInstruction = appConfigService.getSystemInstruction();
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
        StringBuilder prompt = new StringBuilder();
        if (systemInstruction != null && !systemInstruction.isBlank()) {
            prompt.append("System Instruction:\n").append(systemInstruction).append("\n\n");
        }
        prompt.append("Context: Vehicle Taxonomy AI matching.\n")
            .append("Active Manufacturers: [").append(makeListing).append("]\n")
            .append("Models by Make (only once per batch):\n").append(modelListing)
            .append("Instructions: For each raw ADP line, respond with JSON { adpId, sdMakeId, sdModelId, score }.");
        return prompt.toString();
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
