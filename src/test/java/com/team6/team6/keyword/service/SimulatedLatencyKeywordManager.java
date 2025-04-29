package com.team6.team6.keyword.service;

import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;

import java.util.List;

/**
 * SimulatedLatencyKeywordManager는 KeywordManager를 상속받아
 * addKeyword 메서드에 인위적인 지연을 추가하여 테스트 환경에서 사용됩니다.
 */
public class SimulatedLatencyKeywordManager extends KeywordManager {
    private final long delayMillis;

    public SimulatedLatencyKeywordManager(long delayMillis) {
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
}