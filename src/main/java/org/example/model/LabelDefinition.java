package org.example.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LabelDefinition {
    private Integer id;
    private String labelName;
    private String description;
    private String category;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public LabelDefinition() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public LabelDefinition(String labelName, String description, String category) {
        this();
        this.labelName = labelName;
        this.description = description;
        this.category = category;
    }
}