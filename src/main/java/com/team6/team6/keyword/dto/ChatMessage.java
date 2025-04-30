package com.team6.team6.keyword.dto;

import java.time.LocalDateTime;

public record ChatMessage(
        MessageType type,
        String nickname,
        String content,
        LocalDateTime timestamp,
        Object data
) {
    public enum MessageType {
        ENTER, REENTER, LEAVE, KEYWORD_RECEIVED, ANALYSIS_RESULT
    }

    // 상수
    private static final String SYSTEM_NICKNAME = "@시스템";
    private static final String ENTER_MESSAGE_FORMAT = "%s님이 입장했습니다.";
    private static final String REENTER_MESSAGE_FORMAT = "%s님이 재입장했습니다.";
    private static final String KEYWORD_RECEIVED_FORMAT = "키워드 '%s'가 성공적으로 수신되었습니다.";
    private static final String ANALYSIS_RESULT_MESSAGE = "키워드 분석 결과가 도착했습니다.";

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
}