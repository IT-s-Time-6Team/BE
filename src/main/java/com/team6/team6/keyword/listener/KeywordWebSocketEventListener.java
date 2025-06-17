package com.team6.team6.keyword.listener;

import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.service.KeywordWebSocketSubscribeService;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
import com.team6.team6.websocket.event.WebSocketDisconnectEvent;
import com.team6.team6.websocket.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 키워드 모드 전용 웹소켓 이벤트 리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KeywordWebSocketEventListener {

    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");

    private final SimpMessageSendingOperations messagingTemplate;
    private final KeywordWebSocketSubscribeService keywordWebSocketSubscribeService;
    private final RoomMemberStateManager roomMemberStateManager;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 대상 destination과 방 키 추출
        String roomKey = extractRoomKeyIfRoomSubscription(headerAccessor.getDestination());
        if (roomKey == null) return;

        // 사용자 인증 정보 추출
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);

        // NORMAL 게임 모드가 아니면 처리하지 않음
        if (!"NORMAL".equals(principal.getGameMode())) {
            return;
        }

        String nickname = principal.getNickname();
        Long roomId = principal.getRoomId();
        Long memberId = principal.getId();

        // 서비스에 사용자 구독 처리 위임
        ChatMessage message = keywordWebSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);

        // 키워드 분석 결과 발행
        keywordWebSocketSubscribeService.publishAnalysisResults(roomKey, roomId);

        log.info("키워드 모드 구독 처리: roomKey={}, nickname={}", roomKey, nickname);
    }

    @EventListener
    public void handleDisconnect(WebSocketDisconnectEvent event) {
        // NORMAL 게임 모드가 아니면 처리하지 않음
        if (!"NORMAL".equals(event.getGameMode())) {
            return;
        }

        UserPrincipal principal = event.getPrincipal();
        String roomKey = event.getRoomKey();
        String nickname = principal.getNickname();

        // 유저 수 조회
        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);

        // 방 떠남 메시지 처리
        ChatMessage leaveMessage = KeywordChatMessage.leave(nickname, onlineUserCount);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", leaveMessage);

        log.info("키워드 모드 연결 해제 처리: roomKey={}, nickname={}", roomKey, nickname);
    }

    /**
     * 대상 URL이 채팅방 구독 요청인지 확인하고, 방 키를 추출
     */
    private String extractRoomKeyIfRoomSubscription(String destination) {
        if (destination == null) return null;

        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
        return matcher.matches() ? matcher.group(1) : null;
    }
} 