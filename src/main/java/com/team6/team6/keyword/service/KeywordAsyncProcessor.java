package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.GlobalKeywordManager;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordAsyncProcessor {

    private final RoomKeywordManager roomKeywordManager;
    private final GlobalKeywordManager globalKeywordManager;
    private final MessagePublisher messagePublisher;

    @Async
    public void processKeywordAnalysisAsync(Long roomId, String roomKey, String newKeyword) {
        // 키워드 분석 실행
        log.info("키워드 비동기 분석 시작: roomId={}, 키워드={}", roomId, newKeyword);
        List<AnalysisResult> results = roomKeywordManager.addKeyword(roomId, newKeyword);

        // newKeyword가 포함된 그룹 추출
        List<AnalysisResult> targetResults = results.stream()
                .filter(result -> result.variations().contains(newKeyword))
                .toList();

        if (targetResults.isEmpty()) {
            log.warn("키워드 '{}' 관련 분석 결과가 없습니다", newKeyword);
            return;
        }

        if (targetResults.size() > 1) {
            log.debug("키워드 '{}'가 여러 그룹에 속합니다. 그룹 수: {}", newKeyword, targetResults.size());
        }

        // newKeyword를 정규화하여 그룹에 추가
        globalKeywordManager.normalizeKeyword(targetResults.get(0), newKeyword);

        // 분석 완료 결과 브로드캐스팅
        messagePublisher.publishKeywordAnalysisResult(roomKey, results);
        log.info("키워드 분석 완료 및 결과 브로드캐스팅: roomKey={}, 키워드={}", roomKey, newKeyword);
    }
}
