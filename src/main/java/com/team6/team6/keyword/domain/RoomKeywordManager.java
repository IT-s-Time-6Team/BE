package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.domain.repository.GlobalKeywordRepository;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.Keyword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomKeywordManager {

    private final KeywordRepository keywordRepository;
    private final KeywordSimilarityAnalyser keywordSimilarityAnalyser;
    private final AnalysisResultStore analysisResultStore;
    private final GlobalKeywordRepository globalKeywordRepository;
    private final KeywordPreprocessor keywordPreprocessor;

    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
        log.info("방 {}에 키워드 '{}' 추가 시작", roomId, keyword);

        List<String> referenceNames = analysisResultStore.findReferenceNamesByRoomId(roomId, 1);

        if (!referenceNames.isEmpty()) {
            // 전처리된 키워드들로 검색
            List<String> preprocessedReferenceNames = referenceNames.stream()
                    .map(keywordPreprocessor::preprocess)
                    .toList();
            String preprocessedKeyword = keywordPreprocessor.preprocess(keyword);

            Optional<GlobalKeyword> existingGroupKeyword = globalKeywordRepository
                    .findByKeywordInAndSameGroupAs(preprocessedReferenceNames, preprocessedKeyword);

            if (existingGroupKeyword.isPresent()) {
                log.info("키워드 '{}'가 기존 그룹에 추가됨 - 분석 생략", keyword);
                return updateAnalysisResultWithNewKeyword(roomId, keyword, existingGroupKeyword.get().getKeyword());
            }
        }

        // 기존 그룹에 추가되지 않은 경우 새로 분석
        log.info("키워드 '{}'가 새롭게 추가되어 그룹화 실행", keyword);
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
        List<String> keywordsInStore = findKeywordsByRoomId(roomId);
        log.debug("분석 대상 키워드 수: {}", keywordsInStore.size());

        List<AnalysisResult> results;
        if (keywordsInStore.size() <= 1) {
            // 키워드가 1개 이하면 그룹화 생략
            results = keywordsInStore.stream()
                    .map(keyword -> AnalysisResult.of(keyword, List.of(keyword)))
                    .toList();
            log.debug("키워드 수가 1개 이하로 그룹화 생략");
        } else {
            // 키워드가 2개 이상일 때만 그룹화 수행
            List<List<String>> groupedResult = keywordSimilarityAnalyser.analyse(keywordsInStore);
            results = convertToAnalysisResult(groupedResult, keywordsInStore);
        }
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



    private List<AnalysisResult> updateAnalysisResultWithNewKeyword(Long roomId, String newKeyword, String matchingPreprocessedKeyword) {
        log.debug("기존 분석 결과에 키워드 '{}' 추가", newKeyword);

        List<AnalysisResult> existingResults = analysisResultStore.findByRoomId(roomId);

        List<AnalysisResult> updatedResults = existingResults.stream()
                .map(result -> {
                    if (keywordPreprocessor.preprocess(result.referenceName()).equals(matchingPreprocessedKeyword)) {
                        List<String> updatedVariations = new ArrayList<>(result.variations());
                        updatedVariations.add(newKeyword);
                        return new AnalysisResult(result.referenceName(),
                                result.count() + 1,
                                updatedVariations);
                    }
                    return result;
                })
                .toList();

        analysisResultStore.save(roomId, updatedResults);
        log.debug("기존 분석 결과 업데이트 완료");

        return updatedResults;
    }

    private List<String> findKeywordsByRoomId(Long roomId) {
        return keywordRepository.findByRoomId(roomId)
                .stream()
                .map(Keyword::getKeyword)
                .toList();
    }
}
