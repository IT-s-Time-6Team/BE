package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeywordManager {

    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
        return List.of(AnalysisResult.of("Keyword1", List.of("variation1", "variation2")),
                AnalysisResult.of("Keyword2", List.of("variation3", "variation4")));
    }
}
