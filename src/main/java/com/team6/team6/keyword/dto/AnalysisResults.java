package com.team6.team6.keyword.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record AnalysisResults(List<AnalysisResult> results) implements Serializable {

    public static AnalysisResults of(List<AnalysisResult> results) {
        return new AnalysisResults(results);
    }

    public static AnalysisResults empty() {
        return new AnalysisResults(new ArrayList<>());
    }

    public List<String> findSharedKeywords(Integer requiredAgreements) {
        return results.stream()
                .filter(result -> result.count() >= requiredAgreements)
                .flatMap(result -> result.variations().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> findReferenceNames(Integer requiredAgreements) {
        return results.stream()
                .filter(result -> result.count() >= requiredAgreements)
                .map(AnalysisResult::referenceName)
                .distinct()
                .collect(Collectors.toList());
    }
}
