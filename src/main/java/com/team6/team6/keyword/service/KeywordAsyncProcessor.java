package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordAsyncProcessor {

    private final KeywordManager keywordManager;
    private final MessagePublisher messagePublisher;

    @Async
    public void processKeywordAnalysisAsync(Long roomId,String roomKey, String keyword) {
        // 키워드 분석 실행
        List<AnalysisResult> results = keywordManager.addKeyword(roomId, keyword);

        // 분석 완료 후 결과 브로드캐스팅
        messagePublisher.publishKeywordAnalysisResult(roomKey, results);
    }
}