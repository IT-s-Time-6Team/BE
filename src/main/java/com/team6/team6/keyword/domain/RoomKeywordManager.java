package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomKeywordManager {

    private final KeywordStore keywordStore;
    private final KeywordSimilarityAnalyser keywordSimilarityAnalyser;
    private final AnalysisResultStore analysisResultStore;

    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
        // 추가된 키워드 저장
        log.info("방 {}에 키워드 '{}' 추가 시작", roomId, keyword);
        keywordStore.saveKeyword(roomId, keyword);

        // 키워드 추가 시에는 항상 새로 분석
        List<AnalysisResult> results = analyzeAndSave(roomId);
        log.info("방 {}에 키워드 추가 및 분석 완료: 분석 결과 그룹 수={}", roomId, results.size());

        return results;
    }

    // 분석 결과를 가져온다.
    public List<AnalysisResult> getAnalysisResult(Long roomId) {
        return analysisResultStore.findByRoomId(roomId);
    }


    public List<AnalysisResult> analyzeKeywords(Long roomId) {
        // 먼저 analysisResultStore에서 결과가 있는지 확인
        List<AnalysisResult> storedResults = analysisResultStore.findByRoomId(roomId);
        // 저장된 결과가 있으면 그대로 반환
        if (!storedResults.isEmpty()) {
            return storedResults;
        }
        // 저장된 결과가 없으면 새로 분석
        return analyzeAndSave(roomId);
    }

    private List<AnalysisResult> analyzeAndSave(Long roomId) {
        log.debug("방 {} 키워드 분석 및 저장 시작", roomId);
        List<String> keywordsInStore = keywordStore.getKeywords(roomId);
        log.debug("분석 대상 키워드 수: {}", keywordsInStore.size());

        List<List<String>> groupedResult = keywordSimilarityAnalyser.analyse(keywordsInStore);
        List<AnalysisResult> results = convertToAnalysisResult(groupedResult, keywordsInStore);
        // 분석 결과 저장
        analysisResultStore.save(roomId, results);
        log.debug("방 {} 분석 결과 저장 완료: 그룹 수={}", roomId, results.size());

        return results;
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
