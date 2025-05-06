package com.team6.team6.global.security;

import com.team6.team6.member.security.UserPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.util.AntPathMatcher;

import java.util.function.Supplier;

public class RoomAccessAuthorizationManager implements AuthorizationManager<MessageAuthorizationContext<?>> {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MessageAuthorizationContext<?> context) {
        Message<?> message = context.getMessage();
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // CONNECT 명령은 항상 허용 (인증은 이미 SecurityConfig에서 처리됨)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            return new AuthorizationDecision(true);
        }

        String destination = accessor.getDestination();

        if (destination == null || !authentication.get().isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // 사용자 정보 추출
        Authentication auth = authentication.get();
        if (!(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return new AuthorizationDecision(false);
        }

        // URL에서 roomKey 추출
        String roomKeyPattern = "/topic/room/{roomKey}/messages";
        if (destination.startsWith("/app")) {
            roomKeyPattern = "/app/room/{roomKey}/keyword";
        }

        String roomKey = extractRoomKeyFromPath(roomKeyPattern, destination);
        if (roomKey.isEmpty()) {
            return new AuthorizationDecision(false);
        }

        // 현재 사용자의 roomKey와 대상 roomKey 비교
        boolean hasAccess = principal.getRoomKey().equals(roomKey);

        return new AuthorizationDecision(hasAccess);
    }

    private String extractRoomKeyFromPath(String pattern, String path) {
        try {
            return pathMatcher.extractUriTemplateVariables(pattern, path).get("roomKey");
        } catch (Exception e) {
            return "";
        }
    }
}