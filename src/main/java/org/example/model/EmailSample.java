package org.example.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailSample {
    private Integer id;
    private String content;
    private String label;
    private Double confidenceScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EmailSample() {
        this.confidenceScore = 1.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public EmailSample(String content, String label) {
        this();
        this.content = content;
        this.label = label;
    }
}
