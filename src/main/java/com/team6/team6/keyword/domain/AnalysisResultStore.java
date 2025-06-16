package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;

import java.util.List;

public interface AnalysisResultStore {
    void save(Long roomId, List<AnalysisResult> analysisResults);

    List<AnalysisResult> findByRoomId(Long roomId);

    List<String> findSharedKeywordsByRoomId(Long roomId, Integer requiredAgreements);

    List<String> findReferenceNamesByRoomId(Long roomId, Integer requiredAgreements);

    void deleteByRoomId(Long roomId);
}
