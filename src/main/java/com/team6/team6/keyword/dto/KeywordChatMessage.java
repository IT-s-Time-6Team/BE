package com.team6.team6.keyword.dto;

import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

import java.util.List;

/**
 * 키워드 도메인 전용 메시지 클래스
 * ChatMessage를 상속받아 모든 공통 기능을 자동으로 사용 가능
 */
@Slf4j
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
    public static ChatMessage enter(String nickname, int userCount, List<RoomMemberInfo> roomMembers) {
        log.debug("키워드 입장 메시지 생성: nickname={}, userCount={}, memberCount={}",
                nickname, userCount, roomMembers.size());

        ChatMessage message = of(TYPE_ENTER, nickname, String.format("%s님이 입장했습니다.", nickname),
                new UserCountData(userCount, roomMembers));

        log.debug("키워드 입장 메시지 생성 완료: type={}, nickname={}, content={}, userCount={}, memberCount={}",
                message.getType(), message.getNickname(), message.getContent(), userCount, roomMembers.size());

        return message;
    }

    public static ChatMessage leave(String nickname, int userCount, List<RoomMemberInfo> roomMembers) {
        log.debug("키워드 퇴장 메시지 생성: nickname={}, userCount={}, memberCount={}",
                nickname, userCount, roomMembers.size());

        ChatMessage message = of(TYPE_LEAVE, nickname, String.format("%s님이 퇴장했습니다.", nickname),
                new UserCountData(userCount, roomMembers));

        log.debug("키워드 퇴장 메시지 생성 완료: type={}, nickname={}, content={}, userCount={}, memberCount={}",
                message.getType(), message.getNickname(), message.getContent(), userCount, roomMembers.size());

        return message;
    }

    // 키워드 도메인 전용 기능들
    public static ChatMessage keywordReceived(String nickname, String keyword) {
        log.debug("키워드 수신 확인 메시지 생성: nickname={}, keyword={}", nickname, keyword);

        String content = String.format(KEYWORD_RECEIVED_FORMAT, keyword);
        ChatMessage message = of(TYPE_KEYWORD_RECEIVED, nickname, content);

        log.debug("키워드 수신 확인 메시지 생성 완료: type={}, nickname={}, content={}, keyword={}",
                message.getType(), message.getNickname(), message.getContent(), keyword);

        return message;
    }

    public static ChatMessage analysisResult(Object results) {
        log.debug("키워드 분석 결과 메시지 생성: resultsType={}",
                results != null ? results.getClass().getSimpleName() : "null");

        ChatMessage message = of(TYPE_ANALYSIS_RESULT, SYSTEM_NICKNAME, ANALYSIS_RESULT_MESSAGE, results);

        log.debug("키워드 분석 결과 메시지 생성 완료: type={}, nickname={}, content={}",
                message.getType(), message.getNickname(), message.getContent());

        return message;
    }

    public static ChatMessage reenter(String nickname, int userCount, List<String> keywords, List<RoomMemberInfo> roomMembers) {
        log.debug("키워드 재입장 메시지 생성: nickname={}, userCount={}, keywordCount={}, memberCount={}",
                nickname, userCount, keywords != null ? keywords.size() : 0, roomMembers.size());

        String content = String.format(REENTER_MESSAGE_FORMAT, nickname);
        ChatMessage message = of(TYPE_REENTER, nickname, content, new ReenterData(userCount, keywords, roomMembers));

        log.debug("키워드 재입장 메시지 생성 완료: type={}, nickname={}, content={}, userCount={}, keywords={}, memberCount={}",
                message.getType(), message.getNickname(), message.getContent(), userCount, keywords, roomMembers.size());

        return message;
    }

    // 키워드 전용 에러 처리
    public static ChatMessage keywordError(Exception exception) {
        log.debug("키워드 에러 메시지 생성: exceptionType={}, message={}",
                exception.getClass().getSimpleName(), exception.getMessage());

        String content = ERROR_PREFIX + (exception instanceof MethodArgumentNotValidException
                ? KEYWORD_REQUEST_FORMAT_ERROR_MESSAGE
                : SERVER_ERROR_MESSAGE);

        ChatMessage message = of(TYPE_ERROR, SYSTEM_NICKNAME, content);

        log.debug("키워드 에러 메시지 생성 완료: type={}, nickname={}, content={}, exceptionType={}",
                message.getType(), message.getNickname(), message.getContent(),
                exception.getClass().getSimpleName());

        return message;
    }

    // 키워드 도메인에서 필요한 데이터 클래스들
    public record UserCountData(int userCount, List<RoomMemberInfo> roomMembers) {
    }

    public record ReenterData(int userCount, List<String> keywords, List<RoomMemberInfo> roomMembers) {
    }

    // 방 멤버 정보를 담는 클래스
    public record RoomMemberInfo(String nickname, CharacterType character, boolean isLeader) {
    }
} 