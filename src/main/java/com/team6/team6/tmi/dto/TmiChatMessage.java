package com.team6.team6.tmi.dto;

import com.team6.team6.websocket.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

import java.time.LocalDateTime;

@Slf4j
public class TmiChatMessage extends ChatMessage {

    public static final String TYPE_TMI_RECEIVED = "TMI_RECEIVED";
    public static final String TYPE_TMI_COLLECTION_PROGRESS = "TMI_COLLECTION_PROGRESS";
    public static final String TYPE_TMI_COLLECTION_COMPLETED = "TMI_COLLECTION_COMPLETED";
    public static final String TYPE_TMI_VOTING_STARTED = "TMI_VOTING_STARTED";
    public static final String TYPE_TMI_VOTING_PROGRESS = "TMI_VOTING_PROGRESS";
    public static final String TYPE_TMI_ROUND_COMPLETED = "TMI_ROUND_COMPLETED";
    public static final String TYPE_TMI_ALL_COMPLETED = "TMI_ALL_COMPLETED";

    private static final String TMI_RECEIVED_FORMAT = "TMI '%s'가 성공적으로 제출되었습니다.";
    private static final String TMI_COLLECTION_PROGRESS_FORMAT = "TMI 수집 진행률: %d";
    private static final String TMI_COLLECTION_COMPLETED_MESSAGE = "모든 TMI 수집이 완료되었습니다! 투표를 준비해주세요.";
    private static final String TMI_VOTING_STARTED_FORMAT = "다음 TMI에 대한 투표가 시작되었습니다";
    private static final String TMI_VOTING_PROGRESS_FORMAT = "현재 라운드 투표 진행률: %d%%";
    private static final String TMI_ROUND_COMPLETED_FORMAT = "%d번째 TMI 투표가 완료되었습니다.";
    private static final String TMI_ALL_COMPLETED_MESSAGE = "모든 TMI 투표가 완료되었습니다! 결과를 확인해주세요.";

    public TmiChatMessage(String type, String nickname, String content, LocalDateTime timestamp, Object data) {
        super(type, nickname, content, timestamp, data);
    }

    public static ChatMessage tmiReceived(String nickname, String tmiContent) {
        log.debug("TMI 수신 확인 메시지 생성: nickname={}, tmiContent={}", nickname, tmiContent);

        String content = String.format(TMI_RECEIVED_FORMAT, tmiContent);
        ChatMessage message = of(TYPE_TMI_RECEIVED, nickname, content);

        log.debug("TMI 수신 확인 메시지 생성 완료: type={}, nickname={}, content={}",
                message.getType(), message.getNickname(), message.getContent());

        return message;
    }

    public static ChatMessage tmiCollectionProgress(Integer progress) {
        log.debug("TMI 수집 진행률 메시지 생성: progress={}", progress);

        String content = String.format(TMI_COLLECTION_PROGRESS_FORMAT, progress);
        ChatMessage message = of(TYPE_TMI_COLLECTION_PROGRESS, SYSTEM_NICKNAME, content, progress);

        log.debug("TMI 수집 진행률 메시지 생성 완료: type={}, content={}, progress={}",
                message.getType(), message.getContent(), progress);

        return message;
    }

    public static ChatMessage tmiCollectionCompleted() {
        log.debug("TMI 수집 완료 메시지 생성");

        ChatMessage message = of(TYPE_TMI_COLLECTION_COMPLETED, SYSTEM_NICKNAME, TMI_COLLECTION_COMPLETED_MESSAGE);

        log.debug("TMI 수집 완료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage tmiVotingStarted() {
        log.debug("TMI 투표 시작 메시지 생성");

        String content = String.format(TMI_VOTING_STARTED_FORMAT);
        ChatMessage message = of(TYPE_TMI_VOTING_STARTED, SYSTEM_NICKNAME, content);

        log.debug("TMI 투표 시작 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage tmiVotingProgress(Integer progress) {
        log.debug("TMI 투표 진행률 메시지 생성: progress={}", progress);

        String content = String.format(TMI_VOTING_PROGRESS_FORMAT, progress);
        ChatMessage message = of(TYPE_TMI_VOTING_PROGRESS, SYSTEM_NICKNAME, content, progress);

        log.debug("TMI 투표 진행률 메시지 생성 완료: type={}, content={}, progress={}",
                message.getType(), message.getContent(), progress);

        return message;
    }

    public static ChatMessage tmiRoundVotingCompleted(int round) {
        log.debug("TMI 라운드 투표 완료 메시지 생성: round={}", round);

        String content = String.format(TMI_ROUND_COMPLETED_FORMAT, round + 1);
        ChatMessage message = of(TYPE_TMI_ROUND_COMPLETED, SYSTEM_NICKNAME, content, round);

        log.debug("TMI 라운드 투표 완료 메시지 생성 완료: type={}, content={}, round={}",
                message.getType(), message.getContent(), round);

        return message;
    }

    public static ChatMessage tmiAllVotingCompleted() {
        log.debug("TMI 전체 투표 완료 메시지 생성");

        ChatMessage message = of(TYPE_TMI_ALL_COMPLETED, SYSTEM_NICKNAME, TMI_ALL_COMPLETED_MESSAGE);

        log.debug("TMI 전체 투표 완료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage tmiError(Exception exception) {
        log.debug("TMI 에러 메시지 생성: exceptionType={}, message={}",
                exception.getClass().getSimpleName(), exception.getMessage());

        String content = ERROR_PREFIX + (exception instanceof MethodArgumentNotValidException
                ? "잘못된 TMI 형식입니다. TMI 내용을 확인해주세요."
                : SERVER_ERROR_MESSAGE);

        ChatMessage message = of(TYPE_ERROR, SYSTEM_NICKNAME, content);

        log.debug("TMI 에러 메시지 생성 완료: type={}, content={}, exceptionType={}",
                message.getType(), message.getContent(), exception.getClass().getSimpleName());

        return message;
    }
}
