package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.AnalysisResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RedisAnalysisResultStore implements AnalysisResultStore {

    private static final String KEY_PREFIX = "analysis_result:";
    private final RedisTemplate<String, AnalysisResults> redisTemplate;

    @Override
    public void save(Long roomId, List<AnalysisResult> analysisResults) {
        try {
            String key = generateKey(roomId);
            redisTemplate.opsForValue().set(key, AnalysisResults.of(analysisResults));
        } catch (Exception e) {
            log.error("분석 결과를 Redis에 저장하는 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("분석 결과 저장에 실패했습니다.", e);
        }
    }

    @Override
    public List<AnalysisResult> findByRoomId(Long roomId) {
        return getAnalysisResults(roomId).results();
    }

    @Override
    public List<String> findSharedKeywordsByRoomId(Long roomId, Integer requiredAgreements) {
        return getAnalysisResults(roomId).findSharedKeywords(requiredAgreements);
    }

    @Override
    public List<String> findReferenceNamesByRoomId(Long roomId, Integer requiredAgreements) {
        return getAnalysisResults(roomId).findReferenceNames(requiredAgreements);
    }

    @Override
    public void deleteByRoomId(Long roomId) {
        String key = generateKey(roomId);
        redisTemplate.delete(key);
    }

    private AnalysisResults getAnalysisResults(Long roomId) {
        String key = generateKey(roomId);
        try {
            AnalysisResults results = redisTemplate.opsForValue().get(key);
            return results != null ? results : AnalysisResults.empty();
        } catch (Exception e) {
            log.error("분석 결과를 Redis에서 가져오는 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("분석 결과 조회에 실패했습니다.", e);
        }
    }

    private String generateKey(Long roomId) {
        return KEY_PREFIX + roomId;
    }
}
