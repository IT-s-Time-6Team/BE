package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.entity.Keyword;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final KeywordManager keywordManager;
    private final MessagePublisher messagePublisher;

    public Keyword addKeyword(Long roomId, String keyword, Long memberId) {
        // 1. 키워드 저장 및 결과 즉시 반환
        Keyword savedKeyword = keywordRepository.save(Keyword.of(keyword, roomId, memberId));

        // 2. 비동기적으로 키워드 분석 실행
        processKeywordAnalysisAsync(roomId, keyword);

        return savedKeyword;
    }

    @Async
    protected void processKeywordAnalysisAsync(Long roomId, String keyword) {
        // 키워드 분석 실행
        List<AnalysisResult> results = keywordManager.addKeyword(roomId, keyword);

        // 분석 완료 후 결과 브로드캐스팅
        messagePublisher.publishKeywordAnalysisResult(roomId, results);
    }
}