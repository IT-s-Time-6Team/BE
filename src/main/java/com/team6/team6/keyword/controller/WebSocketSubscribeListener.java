package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.dto.ChatMessage;
import com.team6.team6.member.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final SimpMessageSendingOperations messagingTemplate;
    // 사용자 방 입장 기록을 위한 맵 (방ID -> {멤버ID -> 입장 여부})
    private final Map<String, Map<String, Boolean>> roomMemberRegistry = new ConcurrentHashMap<>();

    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 대상 destination과 방 키 추출
        String roomKey = extractRoomKeyFromDestination(headerAccessor.getDestination());
        if (roomKey == null) return;

        // 사용자 인증 정보 및 닉네임 추출
        UserPrincipal principal = extractUserPrincipal(headerAccessor);
        if (principal == null) return;

        String nickname = principal.getNickname();

        // 방에 대한 멤버 레지스트리 조회 또는 생성
        Map<String, Boolean> memberRegistry = roomMemberRegistry.computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>());

        // 입장 또는 재입장 메시지 생성
        ChatMessage message = memberRegistry.containsKey(nickname) ?
                ChatMessage.reenter(nickname) :
                ChatMessage.enter(nickname);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);

        // 사용자 입장 기록
        memberRegistry.put(nickname, true);
    }

    /**
     * 목적지 URL에서 방 키를 추출
     */
    private String extractRoomKeyFromDestination(String destination) {
        if (destination == null) return null;

        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
        return matcher.matches() ? matcher.group(1) : null;
    }

    /**
     * 헤더 접근자에서 사용자 정보 추출
     */
    private UserPrincipal extractUserPrincipal(StompHeaderAccessor headerAccessor) {
        return Optional.ofNullable(headerAccessor.getUser())
                .filter(Authentication.class::isInstance)
                .map(Authentication.class::cast)
                .map(Authentication::getPrincipal)
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .orElse(null);
    }
}