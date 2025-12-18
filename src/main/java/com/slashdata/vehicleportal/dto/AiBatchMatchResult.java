package com.slashdata.vehicleportal.dto;

public class AiBatchMatchResult {

    private int totalCandidates;
    private int suggestionsCreated;
    private int highConfidenceApplied;
    private String promptPreview;
    private java.util.List<AiBatchSuggestion> suggestions;

    public AiBatchMatchResult(int totalCandidates, int suggestionsCreated, int highConfidenceApplied,
                              String promptPreview) {
        this.totalCandidates = totalCandidates;
        this.suggestionsCreated = suggestionsCreated;
        this.highConfidenceApplied = highConfidenceApplied;
        this.promptPreview = promptPreview;
    }

    public AiBatchMatchResult(java.util.List<AiBatchSuggestion> suggestions, int totalCandidates,
                              String promptPreview) {
        this.suggestions = suggestions;
        this.totalCandidates = totalCandidates;
        this.suggestionsCreated = suggestions != null ? suggestions.size() : 0;
        this.highConfidenceApplied = 0;
        this.promptPreview = promptPreview;
    }

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public int getSuggestionsCreated() {
        return suggestionsCreated;
    }

    public void setSuggestionsCreated(int suggestionsCreated) {
        this.suggestionsCreated = suggestionsCreated;
    }

    public int getHighConfidenceApplied() {
        return highConfidenceApplied;
    }

    public void setHighConfidenceApplied(int highConfidenceApplied) {
        this.highConfidenceApplied = highConfidenceApplied;
    }

    public String getPromptPreview() {
        return promptPreview;
    }

    public void setPromptPreview(String promptPreview) {
        this.promptPreview = promptPreview;
    }

    public java.util.List<AiBatchSuggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(java.util.List<AiBatchSuggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
