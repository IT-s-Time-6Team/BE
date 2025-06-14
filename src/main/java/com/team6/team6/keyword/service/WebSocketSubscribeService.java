package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KewordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket 구독 관련 서비스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class WebSocketSubscribeService {

    private final MemberRegistryRepository memberRegistryRepository;
    private final KeywordManager keywordManager;
    private final MessagePublisher messagePublisher;
    private final KeywordService keywordService;


    public KewordChatMessage handleUserSubscription(String roomKey, String nickname, Long roomId, Long memberId) {
        boolean isReenter = memberRegistryRepository.isUserInRoom(roomKey, nickname);

        return isReenter
                ? handleReenter(roomKey, nickname, roomId, memberId)
                : handleEnter(roomKey, nickname);
    }

    private KewordChatMessage handleEnter(String roomKey, String nickname) {
        memberRegistryRepository.registerUserInRoom(roomKey, nickname);
        int onlineUserCount = memberRegistryRepository.getOnlineUserCount(roomKey);
        return KewordChatMessage.enter(nickname, onlineUserCount);
    }

    private KewordChatMessage handleReenter(String roomKey, String nickname, Long roomId, Long memberId) {
        memberRegistryRepository.setUserOnline(roomKey, nickname);
        int onlineUserCount = memberRegistryRepository.getOnlineUserCount(roomKey);
        List<Keyword> keywords = keywordService.getUserKeywords(roomId, memberId);
        List<String> uniqueKeywords = keywords.stream()
                .map(Keyword::getKeyword)
                .distinct()
                .collect(Collectors.toList());
        return KewordChatMessage.reenter(nickname, onlineUserCount, uniqueKeywords);
    }

    /**
     * 키워드 분석 결과를 발행하는 메서드
     *
     * @param roomKey 방 식별자
     * @param roomId  방 ID
     */
    public void publishAnalysisResults(String roomKey, Long roomId) {
        // 마지막 키워드 분석 결과 전송
        List<AnalysisResult> results = keywordManager.getAnalysisResult(roomId);
        if (!results.isEmpty()) {
            messagePublisher.publishKeywordAnalysisResult(roomKey, results);
        }
    }
}