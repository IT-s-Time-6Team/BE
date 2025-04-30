package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeywordManager {

    private final KeywordStore keywordStore;
    private final KeywordSimilarityAnalyser keywordSimilarityAnalyser;

    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
        keywordStore.saveKeyword(roomId, keyword);

        return analyzeKeywords(roomId);
    }

    public List<AnalysisResult> analyzeKeywords(Long roomId) {
        List<String> keywordsInStore = keywordStore.getKeywords(roomId);
        List<List<String>> groupedResult = keywordSimilarityAnalyser.analyse(keywordsInStore);

        return convertToAnalysisResult(groupedResult, keywordsInStore);
    }

    private List<AnalysisResult> convertToAnalysisResult(List<List<String>> groupedResult, List<String> keywordsInStore) {
        return groupedResult.stream()
                .filter(group -> !group.isEmpty()) // 빈 그룹 제외
                .map(group -> {
                    // 그룹에서 keywordsInStore에 가장 먼저 등장한 키워드를 찾기
                    String referenceName = group.stream()
                            .min(Comparator.comparingInt(keywordsInStore::indexOf))
                            .get();
                    return AnalysisResult.of(referenceName, group);
                })
                .toList();
    }
}
