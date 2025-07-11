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
        log.debug("웹소켓 연결 이벤트 수신: sessionId={}", event.getMessage().getHeaders().get("simpSessionId"));

        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        String roomKey = principal.getRoomKey();
        String gameMode = principal.getGameMode();

        log.debug("연결된 사용자 정보: memberId={}, nickname={}, roomKey={}, roomId={}, gameMode={}",
                principal.getId(), principal.getNickname(), roomKey, principal.getRoomId(), gameMode);

        // 유저 온라인 관리
        boolean wasInRoom = roomMemberStateManager.isUserInRoom(roomKey, principal.getNickname());
        roomMemberStateManager.handleUserOnlineStatus(roomKey, principal.getNickname(), true);

        log.debug("사용자 온라인 상태 처리 완료: roomKey={}, nickname={}, wasInRoom={}, isFirstConnection={}",
                roomKey, principal.getNickname(), wasInRoom,
                roomMemberStateManager.isFirstConnection(roomKey, principal.getNickname()));
        
        // 게임 모드별 이벤트 발행
        WebSocketConnectEvent connectEvent = new WebSocketConnectEvent(this, roomKey, principal, gameMode);
        eventPublisher.publishEvent(connectEvent);

        log.debug("웹소켓 연결 이벤트 발행 완료: roomKey={}, nickname={}, gameMode={}",
                roomKey, principal.getNickname(), gameMode);

        log.info("웹소켓 연결: roomKey={}, nickname={}, gameMode={}", roomKey, principal.getNickname(), gameMode);
    }

    @EventListener
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        log.debug("웹소켓 연결 해제 이벤트 수신: sessionId={}", event.getMessage().getHeaders().get("simpSessionId"));

        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        String roomKey = principal.getRoomKey();
        String gameMode = principal.getGameMode();

        log.debug("연결 해제된 사용자 정보: memberId={}, nickname={}, roomKey={}, roomId={}, gameMode={}",
                principal.getId(), principal.getNickname(), roomKey, principal.getRoomId(), gameMode);

        // 유저 오프라인 관리
        boolean wasOnline = roomMemberStateManager.isUserOnline(roomKey, principal.getNickname());
        roomMemberStateManager.handleUserOnlineStatus(roomKey, principal.getNickname(), false);

        log.debug("사용자 오프라인 상태 처리 완료: roomKey={}, nickname={}, wasOnline={}, onlineUserCount={}",
                roomKey, principal.getNickname(), wasOnline,
                roomMemberStateManager.getOnlineUserCount(roomKey));
        
        // 게임 모드별 이벤트 발행
        WebSocketDisconnectEvent disconnectEvent = new WebSocketDisconnectEvent(this, roomKey, principal, gameMode);
        eventPublisher.publishEvent(disconnectEvent);

        log.debug("웹소켓 연결 해제 이벤트 발행 완료: roomKey={}, nickname={}, gameMode={}",
                roomKey, principal.getNickname(), gameMode);

        log.info("웹소켓 연결 해제: roomKey={}, nickname={}, gameMode={}", roomKey, principal.getNickname(), gameMode);
    }
} 