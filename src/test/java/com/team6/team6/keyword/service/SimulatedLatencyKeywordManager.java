package com.team6.team6.keyword.service;

import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.KeywordSimilarityAnalyser;
import com.team6.team6.keyword.domain.KeywordStore;
import com.team6.team6.keyword.dto.AnalysisResult;

import java.util.List;

/**
 * SimulatedLatencyKeywordManager는 KeywordManager를 상속받아
 * addKeyword 메서드에 인위적인 지연을 추가하여 테스트 환경에서 사용됩니다.
 */
public class SimulatedLatencyKeywordManager extends KeywordManager {
    private final long delayMillis;

    public SimulatedLatencyKeywordManager(long delayMillis) {
        super(new MockKeywordStore(), new MockKeywordSimilarityAnalyser(), new MockAnalysisResultStore());
        this.delayMillis = delayMillis;
    }

    @Override
    public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
        try {
            Thread.sleep(delayMillis);
            List<String> variations = List.of("분석결과1", "분석결과2");
            return List.of(AnalysisResult.of("분석결과", variations));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("키워드 분석 중 인터럽트 발생", e);
        }
    }

    // 테스트용 목(mock) 클래스들
    private static class MockKeywordStore implements KeywordStore {
        @Override
        public void saveKeyword(Long roomId, String keyword) {
        }

        @Override
        public List<String> getKeywords(Long roomId) {
            return List.of();
        }
    }

    private static class MockKeywordSimilarityAnalyser implements KeywordSimilarityAnalyser {
        @Override
        public List<List<String>> analyse(List<String> keywords) {
            return List.of();
        }
    }

    private static class MockAnalysisResultStore implements AnalysisResultStore {
        @Override
        public List<AnalysisResult> findByRoomId(Long roomId) {
            return List.of();
        }

        @Override
        public void save(Long roomId, List<AnalysisResult> results) {
        }

        @Override
        public List<String> findSharedKeywordsByRoomId(Long roomId, Integer limit) {
            return List.of();
        }

        @Override
        public List<String> findReferenceNamesByRoomId(Long roomId, Integer limit) {
            return List.of();
        }

        @Override
        public void deleteByRoomId(Long roomId) {
            return;
        }
    }
}
