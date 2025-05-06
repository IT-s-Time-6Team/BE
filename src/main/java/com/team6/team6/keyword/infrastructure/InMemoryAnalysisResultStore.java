package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.keyword.dto.AnalysisResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryAnalysisResultStore implements AnalysisResultStore {

    // 방 ID를 키로 하고, 해당 방의 분석 결과 리스트를 값으로 하는 맵
    private final Map<Long, List<AnalysisResult>> resultStore = new ConcurrentHashMap<>();

    @Override
    public void save(Long roomId, List<AnalysisResult> analysisResults) {
        // 기존 결과를 새 결과로 교체
        resultStore.put(roomId, new ArrayList<>(analysisResults));
    }

    @Override
    public List<AnalysisResult> findByRoomId(Long roomId) {
        // 해당 방의 모든 분석 결과 조회
        return resultStore.getOrDefault(roomId, new ArrayList<>());
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
}
