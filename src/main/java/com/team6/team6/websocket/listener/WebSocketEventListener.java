package com.team6.team6.websocket.listener;

import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.event.WebSocketConnectEvent;
import com.team6.team6.websocket.event.WebSocketDisconnectEvent;
import com.team6.team6.websocket.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final RoomMemberStateManager roomMemberStateManager;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        String roomKey = principal.getRoomKey();
        String gameMode = principal.getGameMode();

        // 유저 온라인 관리
        roomMemberStateManager.handleUserOnlineStatus(roomKey, principal.getNickname(), true);
        
        // 게임 모드별 이벤트 발행
        eventPublisher.publishEvent(new WebSocketConnectEvent(this, roomKey, principal, gameMode));

        log.info("웹소켓 연결: roomKey={}, nickname={}, gameMode={}", roomKey, principal.getNickname(), gameMode);
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        String roomKey = principal.getRoomKey();
        String gameMode = principal.getGameMode();

        // 유저 오프라인 관리
        roomMemberStateManager.handleUserOnlineStatus(roomKey, principal.getNickname(), false);
        
        // 게임 모드별 이벤트 발행
        eventPublisher.publishEvent(new WebSocketDisconnectEvent(this, roomKey, principal, gameMode));

        log.info("웹소켓 연결 해제: roomKey={}, nickname={}, gameMode={}", roomKey, principal.getNickname(), gameMode);
    }
} 