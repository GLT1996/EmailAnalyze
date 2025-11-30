package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchResult {
    private String label;
    private String keyword;
    private Double confidence;
}