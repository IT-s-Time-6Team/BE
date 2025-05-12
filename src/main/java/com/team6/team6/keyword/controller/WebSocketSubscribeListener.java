package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import com.team6.team6.keyword.dto.ChatMessage;
import com.team6.team6.member.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener implements ApplicationListener<SessionSubscribeEvent> {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MemberRegistryRepository memberRegistryRepository;

    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");

    @Override
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 대상 destination과 방 키 추출
        String roomKey = extractRoomKeyIfRoomSubscription(headerAccessor.getDestination());
        if (roomKey == null) return;

        // 사용자 인증 정보 및 닉네임 추출
        UserPrincipal principal = extractUserPrincipal(headerAccessor);
        if (principal == null) return;

        String nickname = principal.getNickname();

        // 사용자가 이미 방에 있는지 확인
        boolean isReenter = memberRegistryRepository.isUserInRoom(roomKey, nickname);

        // 사용자를 온라인으로 설정
        if (isReenter) {
            memberRegistryRepository.setUserOnline(roomKey, nickname);
        } else {
            memberRegistryRepository.registerUserInRoom(roomKey, nickname);
        }

        // 현재 방에 있는 온라인 사용자 수 계산
        int onlineUserCount = memberRegistryRepository.getOnlineUserCount(roomKey);

        // 입장 또는 재입장 메시지 생성
        ChatMessage message = isReenter ?
                ChatMessage.reenter(nickname, onlineUserCount) :
                ChatMessage.enter(nickname, onlineUserCount);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);
    }

    /**
     * 대상 URL이 채팅방 구독 요청인지 확인하고, 방 키를 추출
     */
    private String extractRoomKeyIfRoomSubscription(String destination) {
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
