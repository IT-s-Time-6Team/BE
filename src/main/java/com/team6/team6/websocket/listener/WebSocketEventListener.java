package com.team6.team6.websocket.listener;

import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import com.team6.team6.member.security.UserPrincipal;
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

    private final MemberRegistryRepository memberRegistryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);
        String roomKey = principal.getRoomKey();
        String gameMode = principal.getGameMode();

        // 유저 온라인 관리
        handleUserOnlineStatus(roomKey, principal.getNickname(), true);

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
        handleUserOnlineStatus(roomKey, principal.getNickname(), false);

        // 게임 모드별 이벤트 발행
        eventPublisher.publishEvent(new WebSocketDisconnectEvent(this, roomKey, principal, gameMode));

        log.info("웹소켓 연결 해제: roomKey={}, nickname={}, gameMode={}", roomKey, principal.getNickname(), gameMode);
    }

    private void handleUserOnlineStatus(String roomKey, String nickname, boolean isOnline) {
        if (isOnline) {
            // 연결시 - 방 있음/없음에 따른 처리
            if (memberRegistryRepository.isUserInRoom(roomKey, nickname)) {
                // 기존에 있음 - 온라인으로 변경
                memberRegistryRepository.setUserOnline(roomKey, nickname);
            } else {
                // 없음 - 등록 후 온라인 변경
                memberRegistryRepository.registerUserInRoom(roomKey, nickname);
            }
        } else {
            // 연결 해제시 - 오프라인 전환
            memberRegistryRepository.setUserOffline(roomKey, nickname);
        }
    }
} 