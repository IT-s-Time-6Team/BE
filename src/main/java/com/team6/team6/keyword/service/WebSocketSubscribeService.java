package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
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

    private final RoomMemberStateManager roomMemberStateManager;
    private final KeywordManager keywordManager;
    private final MessagePublisher messagePublisher;
    private final KeywordService keywordService;

    public ChatMessage handleUserSubscription(String roomKey, String nickname, Long roomId, Long memberId) {
        // 첫 연결인지 확인 (연결 시점에서 설정된 상태 확인)
        boolean isFirstConnection = roomMemberStateManager.isFirstConnection(roomKey, nickname);

        // 구독 완료 표시
//        roomMemberStateManager.markSubscriptionCompleted(roomKey, nickname);

        return isFirstConnection
                ? handleEnter(roomKey, nickname)
                : handleReenter(roomKey, nickname, roomId, memberId);
    }

    private ChatMessage handleEnter(String roomKey, String nickname) {
        // 첫 연결이므로 이미 등록되어 있음 (연결 시점에서 처리됨)
        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        return KeywordChatMessage.enter(nickname, onlineUserCount);
    }

    private ChatMessage handleReenter(String roomKey, String nickname, Long roomId, Long memberId) {
        // 재연결이므로 이미 온라인 상태임 (연결 시점에서 처리됨)
        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        List<Keyword> keywords = keywordService.getUserKeywords(roomId, memberId);
        List<String> uniqueKeywords = keywords.stream()
                .map(Keyword::getKeyword)
                .distinct()
                .collect(Collectors.toList());
        return KeywordChatMessage.reenter(nickname, onlineUserCount, uniqueKeywords);
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