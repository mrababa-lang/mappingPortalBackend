package com.slashdata.vehicleportal.dto;

public class AiBatchMatchResult {

    private int totalCandidates;
    private int suggestionsCreated;
    private int highConfidenceApplied;
    private String promptPreview;

    public AiBatchMatchResult(int totalCandidates, int suggestionsCreated, int highConfidenceApplied,
                              String promptPreview) {
        this.totalCandidates = totalCandidates;
        this.suggestionsCreated = suggestionsCreated;
        this.highConfidenceApplied = highConfidenceApplied;
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
}
