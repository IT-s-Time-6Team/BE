package com.team6.team6.keyword.dto;

import org.springframework.messaging.converter.MessageConversionException;

import java.time.LocalDateTime;

public record ChatMessage(
        MessageType type,
        String nickname,
        String content,
        LocalDateTime timestamp,
        Object data
) {
    private static final String ROOM_EXPIRY_WARNING_MESSAGE = "방 종료까지 5분 남았습니다.";

    // 상수
    private static final String SYSTEM_NICKNAME = "@시스템";
    private static final String ENTER_MESSAGE_FORMAT = "%s님이 입장했습니다.";
    private static final String REENTER_MESSAGE_FORMAT = "%s님이 재입장했습니다.";
    private static final String KEYWORD_RECEIVED_FORMAT = "키워드 '%s'가 성공적으로 수신되었습니다.";
    private static final String ANALYSIS_RESULT_MESSAGE = "키워드 분석 결과가 도착했습니다.";
    private static final String ERROR_PREFIX = "[오류] ";
    private static final String REQUEST_FORMAT_ERROR_MESSAGE = "잘못된 요청 형식입니다. 올바른 형식 : { \"keyword\": \"키워드\" }";
    private static final String ROOM_EXPIRED_MESSAGE = "방이 종료되었습니다.";

    public static ChatMessage error(MessageConversionException exception) {
        return new ChatMessage(
                MessageType.ERROR,
                SYSTEM_NICKNAME,
                ERROR_PREFIX + REQUEST_FORMAT_ERROR_MESSAGE,
                LocalDateTime.now(),
                null
        );
    }

    // 빌더 메서드
    public static ChatMessage enter(String nickname) {
        return new ChatMessage(
                MessageType.ENTER,
                nickname,
                String.format(ENTER_MESSAGE_FORMAT, nickname),
                LocalDateTime.now(),
                null
        );
    }

    public static ChatMessage reenter(String nickname) {
        return new ChatMessage(
                MessageType.REENTER,
                nickname,
                String.format(REENTER_MESSAGE_FORMAT, nickname),
                LocalDateTime.now(),
                null
        );
    }

    public static ChatMessage keywordReceived(String nickname, String keyword) {
        return new ChatMessage(
                MessageType.KEYWORD_RECEIVED,
                nickname,
                String.format(KEYWORD_RECEIVED_FORMAT, keyword),
                LocalDateTime.now(),
                null
        );
    }

    public static ChatMessage analysisResult(Object results) {
        return new ChatMessage(
                MessageType.ANALYSIS_RESULT,
                SYSTEM_NICKNAME,
                ANALYSIS_RESULT_MESSAGE,
                LocalDateTime.now(),
                results
        );
    }

    public static ChatMessage roomExpiryWarning() {
        return new ChatMessage(
                MessageType.ROOM_EXPIRY_WARNING,
                SYSTEM_NICKNAME,
                ROOM_EXPIRY_WARNING_MESSAGE,
                LocalDateTime.now(),
                null
        );
    }

    public static ChatMessage roomExpired() {
        return new ChatMessage(
                MessageType.ROOM_EXPIRED,
                SYSTEM_NICKNAME,
                ROOM_EXPIRED_MESSAGE,
                LocalDateTime.now(),
                null
        );
    }

    public enum MessageType {
        ENTER, REENTER, LEAVE, KEYWORD_RECEIVED, ANALYSIS_RESULT, ERROR, ROOM_EXPIRY_WARNING, ROOM_EXPIRED
    }
}