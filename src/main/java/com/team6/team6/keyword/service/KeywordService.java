package com.team6.team6.keyword.service;

import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.KeywordAddServiceReq;
import com.team6.team6.keyword.entity.Keyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final KeywordAsyncProcessor keywordAsyncProcessor;

    public Keyword addKeyword(KeywordAddServiceReq req) {
        // 1. 키워드 저장 및 결과 즉시 반환
        Keyword savedKeyword = keywordRepository.save(req.toEntity());

        // 2. 비동기적으로 키워드 분석 실행 - 다른 서비스에 위임
        keywordAsyncProcessor.processKeywordAnalysisAsync(req.roomId(), req.roomKey(), req.keyword());

        return savedKeyword;
    }
}