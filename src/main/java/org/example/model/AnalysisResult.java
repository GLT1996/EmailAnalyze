package org.example.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalysisResult {
    private Integer id;
    private Integer emailId;
    private String detectedLabel;
    private Double confidenceScore;
    private LocalDateTime analysisTimestamp;

    public AnalysisResult() {
        this.analysisTimestamp = LocalDateTime.now();
    }

    public AnalysisResult(Integer emailId, String detectedLabel, Double confidenceScore) {
        this();
        this.emailId = emailId;
        this.detectedLabel = detectedLabel;
        this.confidenceScore = confidenceScore;
    }
}