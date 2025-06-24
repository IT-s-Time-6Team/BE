package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket 구독 관련 서비스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordWebSocketSubscribeService {

    private final RoomMemberStateManager roomMemberStateManager;
    private final RoomKeywordManager keywordManager;
    private final MessagePublisher messagePublisher;
    private final KeywordService keywordService;

    public ChatMessage handleUserSubscription(String roomKey, String nickname, Long roomId, Long memberId) {
        log.debug("사용자 구독 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        // 첫 연결인지 확인 (연결 시점에서 설정된 상태 확인)
        boolean isFirstConnection = roomMemberStateManager.isFirstConnection(roomKey, nickname);

        log.debug("연결 상태 확인: roomKey={}, nickname={}, isFirstConnection={}",
                roomKey, nickname, isFirstConnection);

        ChatMessage result = isFirstConnection
                ? handleEnter(roomKey, nickname)
                : handleReenter(roomKey, nickname, roomId, memberId);

        log.debug("사용자 구독 처리 완료: roomKey={}, nickname={}, messageType={}",
                roomKey, nickname, result.getType());

        return result;
    }

    private ChatMessage handleEnter(String roomKey, String nickname) {
        log.debug("첫 입장 처리 시작: roomKey={}, nickname={}", roomKey, nickname);

        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);

        log.debug("온라인 사용자 수 조회 완료: roomKey={}, onlineUserCount={}", roomKey, onlineUserCount);

        ChatMessage enterMessage = KeywordChatMessage.enter(nickname, onlineUserCount);

        log.debug("입장 메시지 생성 완료: nickname={}, type={}, content={}",
                nickname, enterMessage.getType(), enterMessage.getContent());

        return enterMessage;
    }

    private ChatMessage handleReenter(String roomKey, String nickname, Long roomId, Long memberId) {
        log.debug("재입장 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);

        log.debug("온라인 사용자 수 조회 완료: roomKey={}, onlineUserCount={}", roomKey, onlineUserCount);

        List<Keyword> keywords = keywordService.getUserKeywords(roomId, memberId);

        log.debug("사용자 키워드 조회 완료: roomId={}, memberId={}, keywordCount={}",
                roomId, memberId, keywords.size());

        List<String> uniqueKeywords = keywords.stream()
                .map(Keyword::getKeyword)
                .distinct()
                .collect(Collectors.toList());

        log.debug("고유 키워드 목록 생성: roomId={}, memberId={}, uniqueKeywordCount={}, keywords={}",
                roomId, memberId, uniqueKeywords.size(), uniqueKeywords);

        ChatMessage reenterMessage = KeywordChatMessage.reenter(nickname, onlineUserCount, uniqueKeywords);

        log.debug("재입장 메시지 생성 완료: nickname={}, type={}, content={}, userCount={}, keywordCount={}",
                nickname, reenterMessage.getType(), reenterMessage.getContent(),
                onlineUserCount, uniqueKeywords.size());

        return reenterMessage;
    }

    /**
     * 키워드 분석 결과를 발행하는 메서드
     *
     * @param roomKey 방 식별자
     * @param roomId  방 ID
     */
    public void publishAnalysisResults(String roomKey, Long roomId) {
        log.debug("키워드 분석 결과 발행 시작: roomKey={}, roomId={}", roomKey, roomId);

        // 마지막 키워드 분석 결과 전송
        List<AnalysisResult> results = keywordManager.getAnalysisResult(roomId);

        log.debug("키워드 분석 결과 조회 완료: roomKey={}, roomId={}, resultCount={}",
                roomKey, roomId, results.size());

        if (!results.isEmpty()) {
            messagePublisher.publishKeywordAnalysisResult(roomKey, results);

            log.debug("키워드 분석 결과 발행 완료: roomKey={}, roomId={}, resultCount={}",
                    roomKey, roomId, results.size());
        } else {
            log.debug("발행할 키워드 분석 결과가 없음: roomKey={}, roomId={}", roomKey, roomId);
        }
    }
}
