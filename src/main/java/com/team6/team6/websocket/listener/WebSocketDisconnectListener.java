package com.team6.team6.websocket.listener;

import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import com.team6.team6.websocket.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MemberRegistryRepository memberRegistryRepository;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        // 사용자 인증 정보 및 닉네임 추출
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        if (principal == null) return;

        String nickname = principal.getNickname();
        String roomKey = principal.getRoomKey();

        // 사용자를 오프라인으로 설정
        memberRegistryRepository.setUserOffline(roomKey, nickname);

        // 현재 방에 있는 온라인 사용자 수 계산
        int onlineUserCount = memberRegistryRepository.getOnlineUserCount(roomKey);

        // 사용자 퇴장 메시지 생성 (키워드 도메인이므로 KeywordChatMessage 사용)
        ChatMessage message = KeywordChatMessage.leave(nickname, onlineUserCount);

        // 메시지 전송
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);
    }
} 