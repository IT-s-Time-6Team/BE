package com.team6.team6.websocket.event;

import com.team6.team6.member.security.UserPrincipal;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 웹소켓 연결 해제 이벤트
 */
@Getter
public class WebSocketDisconnectEvent extends ApplicationEvent {

    private final String roomKey;
    private final UserPrincipal principal;
    private final String gameMode;

    public WebSocketDisconnectEvent(Object source, String roomKey, UserPrincipal principal, String gameMode) {
        super(source);
        this.roomKey = roomKey;
        this.principal = principal;
        this.gameMode = gameMode;
    }
} 