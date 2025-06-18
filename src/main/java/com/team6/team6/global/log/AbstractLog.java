package com.team6.team6.global.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team6.team6.member.security.UserPrincipal;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.slf4j.LoggerFactory.getLogger;

@Getter
public abstract class AbstractLog {

    private static final String ANONYMOUS_USER = "anonymous";
    private static final String UNKNOWN_ROOM = "unknown-room";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    protected static final Logger log = getLogger(AbstractLog.class);

    private final String userId;
    private final String roomKey;
    private final LogType type;
    private final String timestamp;

    protected AbstractLog(LogType type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.userId = getCurrentUserId(authentication);
        this.roomKey = getCurrentRoomKey(authentication);
        this.type = type;
        this.timestamp = getCurrentTimestamp();
    }

    public abstract void output();

    protected String getLogMessage() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize log data", e);
            return "{}";
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static String getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            return principal.getNickname();
        }
        return ANONYMOUS_USER;
    }

    private static String getCurrentRoomKey(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            return userPrincipal.getRoomKey() != null ? userPrincipal.getRoomKey() : UNKNOWN_ROOM;
        }
        return UNKNOWN_ROOM;
    }
}
