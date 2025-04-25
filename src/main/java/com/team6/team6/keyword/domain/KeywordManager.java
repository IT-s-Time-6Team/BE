package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordManager {

    private final KeywordStore keywordStore;
    private final KeywordSimilarityAnalyser keywordSimilarityAnalyser;

    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {

        keywordStore.saveKeyword(roomId, keyword);
        List<String> keywordsInStore = keywordStore.getKeywords(roomId);
        List<List<String>> groupedResult = keywordSimilarityAnalyser.analyse(keywordsInStore);

        return List.of(AnalysisResult.of("Keyword1", List.of("variation1", "variation2")),
                AnalysisResult.of("Keyword2", List.of("variation3", "variation4")));
    }
}
