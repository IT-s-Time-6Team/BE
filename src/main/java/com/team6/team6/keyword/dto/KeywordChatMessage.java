package com.team6.team6.keyword.dto;

import com.team6.team6.websocket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

import java.util.List;

/**
 * 키워드 도메인 전용 메시지 클래스
 * ChatMessage를 상속받아 모든 공통 기능을 자동으로 사용 가능
 */
public class KeywordChatMessage extends ChatMessage {

    // 키워드 전용 메시지 타입
    public static final String TYPE_KEYWORD_RECEIVED = "KEYWORD_RECEIVED";
    public static final String TYPE_ANALYSIS_RESULT = "ANALYSIS_RESULT";
    public static final String TYPE_REENTER = "REENTER";

    // 키워드 전용 메시지 포맷
    private static final String KEYWORD_RECEIVED_FORMAT = "키워드 '%s'가 성공적으로 수신되었습니다.";
    private static final String ANALYSIS_RESULT_MESSAGE = "키워드 분석 결과가 도착했습니다.";
    private static final String REENTER_MESSAGE_FORMAT = "%s님이 재입장했습니다.";
    private static final String KEYWORD_REQUEST_FORMAT_ERROR_MESSAGE = "잘못된 요청 형식입니다. 올바른 형식 : { \"keyword\": \"키워드\" }";

    public KeywordChatMessage(String type, String nickname, String content, java.time.LocalDateTime timestamp, Object data) {
        super(type, nickname, content, timestamp, data);
    }

    // 키워드 도메인에서 필요한 enter/leave - UserCountData 추가
    public static ChatMessage enter(String nickname, int userCount) {
        return of(TYPE_ENTER, nickname, String.format("%s님이 입장했습니다.", nickname), new UserCountData(userCount));
    }

    public static ChatMessage leave(String nickname, int userCount) {
        return of(TYPE_LEAVE, nickname, String.format("%s님이 퇴장했습니다.", nickname), new UserCountData(userCount));
    }

    // 키워드 도메인 전용 기능들
    public static ChatMessage keywordReceived(String nickname, String keyword) {
        return of(TYPE_KEYWORD_RECEIVED, nickname, String.format(KEYWORD_RECEIVED_FORMAT, keyword));
    }

    public static ChatMessage analysisResult(Object results) {
        return of(TYPE_ANALYSIS_RESULT, SYSTEM_NICKNAME, ANALYSIS_RESULT_MESSAGE, results);
    }

    public static ChatMessage reenter(String nickname, int userCount, List<String> keywords) {
        return of(TYPE_REENTER, nickname, String.format(REENTER_MESSAGE_FORMAT, nickname), new ReenterData(userCount, keywords));
    }

    // 키워드 전용 에러 처리
    public static ChatMessage keywordError(Exception exception) {
        return of(TYPE_ERROR, SYSTEM_NICKNAME, ERROR_PREFIX + (exception instanceof MethodArgumentNotValidException
                ? KEYWORD_REQUEST_FORMAT_ERROR_MESSAGE
                : SERVER_ERROR_MESSAGE));
    }

    // 키워드 도메인에서 필요한 데이터 클래스들
    public record UserCountData(int userCount) {
    }

    public record ReenterData(int userCount, List<String> keywords) {
    }
} 