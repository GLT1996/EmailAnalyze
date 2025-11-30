package org.example.model;



import lombok.Data;

import java.util.List;

@Data
public class EmailAnalysis {
    private String emailContent;
    private List<MatchResult> matches;
    private String primaryLabel;
    private Integer matchCount;

    public EmailAnalysis(String emailContent) {
        this.emailContent = emailContent;
        this.primaryLabel = "unknown";
        this.matchCount = 0;
    }
}