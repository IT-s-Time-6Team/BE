package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class RedisAnalysisResultStore implements AnalysisResultStore {

    private static final String KEY_PREFIX = "analysis_result:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(Long roomId, List<AnalysisResult> analysisResults) {
        String key = generateKey(roomId);
        redisTemplate.opsForValue().set(key, new ArrayList<>(analysisResults));
    }

    @Override
    public List<AnalysisResult> findByRoomId(Long roomId) {
        String key = generateKey(roomId);
        List<AnalysisResult> results = (List<AnalysisResult>) redisTemplate.opsForValue().get(key);
        return results != null ? results : new ArrayList<>();
    }

    @Override
    public List<String> findSharedKeywordsByRoomId(Long roomId, Integer requiredAgreements) {
        List<AnalysisResult> results = findByRoomId(roomId);

        return results.stream()
                .filter(result -> result.count() >= requiredAgreements)
                .flatMap(result -> result.variations().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findReferenceNamesByRoomId(Long roomId, Integer requiredAgreements) {
        List<AnalysisResult> results = findByRoomId(roomId);

        return results.stream()
                .filter(result -> result.count() >= requiredAgreements)
                .map(AnalysisResult::referenceName)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByRoomId(Long roomId) {
        String key = generateKey(roomId);
        redisTemplate.delete(key);
    }

    private String generateKey(Long roomId) {
        return KEY_PREFIX + roomId;
    }
}
