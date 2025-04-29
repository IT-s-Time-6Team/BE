package com.team6.team6.keyword.dto;

import java.util.List;

public record AnalysisResult(String referenceName, int count, List<String> variations) {

    public static AnalysisResult of(String referenceName, List<String> variations) {
        return new AnalysisResult(referenceName, variations.size(), variations);
    }
}
