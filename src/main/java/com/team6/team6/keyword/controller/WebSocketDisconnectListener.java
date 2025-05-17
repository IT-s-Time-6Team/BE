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
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MemberRegistryRepository memberRegistryRepository;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 사용자 인증 정보 및 닉네임 추출
        UserPrincipal principal = extractUserPrincipal(headerAccessor);
        if (principal == null) return;

        String nickname = principal.getNickname();
        String roomKey = principal.getRoomKey();

        // 사용자를 오프라인으로 설정
        memberRegistryRepository.setUserOffline(roomKey, nickname);

        // 현재 방에 있는 온라인 사용자 수 계산
        int onlineUserCount = memberRegistryRepository.getOnlineUserCount(roomKey);

        // 사용자 퇴장 메시지 생성
        ChatMessage message = ChatMessage.leave(nickname, onlineUserCount);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);
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
