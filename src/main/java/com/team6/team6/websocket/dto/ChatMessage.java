package com.team6.team6.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessage {

    // 공통 상수들
    public static final String SYSTEM_NICKNAME = "@시스템";
    public static final String ERROR_PREFIX = "[오류] ";
    public static final String SERVER_ERROR_MESSAGE = "서버 오류가 발생했습니다.";
    // 공통 메시지 타입
    public static final String TYPE_ENTER = "ENTER";
    public static final String TYPE_LEAVE = "LEAVE";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_ROOM_EXPIRY_WARNING = "ROOM_EXPIRY_WARNING";
    public static final String TYPE_ROOM_EXPIRED = "ROOM_EXPIRED";
    public static final String TYPE_LEADER_ROOM_EXPIRED = "LEADER_ROOM_EXPIRED";
    public static final String TYPE_KEY_EVENT = "KEY_EVENT";
    // 공통 메시지 포맷
    private static final String ENTER_MESSAGE_FORMAT = "%s님이 입장했습니다.";
    private static final String LEAVE_MESSAGE_FORMAT = "%s님이 퇴장했습니다.";
    private static final String ROOM_EXPIRED_MESSAGE = "방이 종료되었습니다.";
    private static final String LEADER_ROOM_EXPIRED_MESSAGE = "방장이 방을 종료했습니다.";
    private static final String ROOM_EXPIRY_WARNING_MESSAGE = "방 종료까지 5분 남았습니다.";
    private final String type;
    private final String nickname;
    private final String content;
    private final LocalDateTime timestamp;
    private final Object data;

    // 기본 생성 메서드
    public static ChatMessage of(String type, String nickname, String content, Object data) {
        return new ChatMessage(type, nickname, content, LocalDateTime.now(), data);
    }

    public static ChatMessage of(String type, String nickname, String content) {
        return new ChatMessage(type, nickname, content, LocalDateTime.now(), null);
    }

    // 공통 기능들 - 기본 파라미터만, data는 null
    public static ChatMessage error(Exception exception) {
        return of(TYPE_ERROR, SYSTEM_NICKNAME, ERROR_PREFIX + SERVER_ERROR_MESSAGE);
    }

    public static ChatMessage enter(String nickname) {
        return of(TYPE_ENTER, nickname, String.format(ENTER_MESSAGE_FORMAT, nickname));
    }

    public static ChatMessage leave(String nickname) {
        return of(TYPE_LEAVE, nickname, String.format(LEAVE_MESSAGE_FORMAT, nickname));
    }

    public static ChatMessage roomExpiryWarning() {
        return of(TYPE_ROOM_EXPIRY_WARNING, SYSTEM_NICKNAME, ROOM_EXPIRY_WARNING_MESSAGE);
    }

    public static ChatMessage roomExpired() {
        return of(TYPE_ROOM_EXPIRED, SYSTEM_NICKNAME, ROOM_EXPIRED_MESSAGE);
    }

    public static ChatMessage leaderRoomExpired() {
        return of(TYPE_LEADER_ROOM_EXPIRED, SYSTEM_NICKNAME, LEADER_ROOM_EXPIRED_MESSAGE);
    }

    public static ChatMessage keyEvent(String nickname, String keyEvent) {
        return of(TYPE_KEY_EVENT, nickname, keyEvent);
    }
} 