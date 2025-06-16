package com.team6.team6.websocket.listener;

import com.team6.team6.keyword.service.WebSocketSubscribeService;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import com.team6.team6.websocket.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");
    private final SimpMessageSendingOperations messagingTemplate;
    private final WebSocketSubscribeService webSocketSubscribeService;

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 대상 destination과 방 키 추출
        String roomKey = extractRoomKeyIfRoomSubscription(headerAccessor.getDestination());
        if (roomKey == null) return;

        // 사용자 인증 정보 및 닉네임 추출
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        if (principal == null) return;

        String nickname = principal.getNickname();
        Long roomId = principal.getRoomId();
        Long memberId = principal.getId();

        // 서비스에 사용자 구독 처리 위임
        ChatMessage message = webSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);

        // 키워드 분석 결과 발행
        webSocketSubscribeService.publishAnalysisResults(roomKey, roomId);
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