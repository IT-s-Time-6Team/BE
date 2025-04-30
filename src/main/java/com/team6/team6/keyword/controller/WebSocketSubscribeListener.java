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
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final SimpMessageSendingOperations messagingTemplate;
    // 사용자 방 입장 기록을 위한 맵 (방ID -> {멤버ID -> 입장 여부})
    private final Map<Long, Map<Long, Boolean>> roomMemberRegistry = new ConcurrentHashMap<>();
    
    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        
        // 채팅방 구독인지 확인
        if (destination == null) return;
        
        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) return;
        
        String roomKey = matcher.group(1);
        
        // 인증 정보 가져오기
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) return;

        Long memberId = principal.getId();
        String nickname = principal.getNickname();
        Long roomId = principal.getRoomId();

        // 방에 대한 멤버 레지스트리 조회 또는 생성
        Map<Long, Boolean> memberRegistry = roomMemberRegistry.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        // 채팅 메시지 생성 및 전송
        ChatMessage message;
        if (memberRegistry.containsKey(memberId)) {
            message = ChatMessage.reenter(nickname);
        } else {
            message = ChatMessage.enter(nickname);
        }

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);

        // 사용자 입장 기록
        memberRegistry.put(memberId, true);
    }
}