package com.team6.team6.websocket.util;

import com.team6.team6.member.security.UserPrincipal;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Optional;

public class WebSocketUtil {

    private WebSocketUtil() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * SessionConnectedEvent에서 사용자 정보 추출
     */
    public static UserPrincipal extractUserPrincipalFromStompHeader(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        return extractUserPrincipalFromStompHeader(headerAccessor);
    }

    /**
     * SessionDisconnectEvent에서 사용자 정보 추출
     */
    public static UserPrincipal extractUserPrincipalFromStompHeader(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        return extractUserPrincipalFromStompHeader(headerAccessor);
    }

    /**
     * SessionSubscribeEvent에서 사용자 정보 추출
     */
    public static UserPrincipal extractUserPrincipalFromStompHeader(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        return extractUserPrincipalFromStompHeader(headerAccessor);
    }

    /**
     * StompHeaderAccessor에서 사용자 정보 추출
     */
    public static UserPrincipal extractUserPrincipalFromStompHeader(StompHeaderAccessor headerAccessor) {
        return Optional.ofNullable(headerAccessor.getUser())
                .filter(Authentication.class::isInstance)
                .map(Authentication.class::cast)
                .map(Authentication::getPrincipal)
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .orElse(null);
    }
} 