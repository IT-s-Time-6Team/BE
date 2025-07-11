package com.team6.team6.balance.dto;

import com.team6.team6.websocket.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class BalanceChatMessage extends ChatMessage {

    // 멤버 입장 대기 단계
    public static final String TYPE_BALANCE_ALL_MEMBERS_JOINED = "BALANCE_ALL_MEMBERS_JOINED";

    // 문제 공개 단계 (30초)
    public static final String TYPE_BALANCE_QUESTION_STARTED = "BALANCE_QUESTION_STARTED";
    public static final String TYPE_BALANCE_QUESTION_TIME_REMAINING = "BALANCE_QUESTION_TIME_REMAINING";
    public static final String TYPE_BALANCE_QUESTION_ENDED = "BALANCE_QUESTION_ENDED";

    // 토론 단계 (5분)
    public static final String TYPE_BALANCE_DISCUSSION_STARTED = "BALANCE_DISCUSSION_STARTED";
    public static final String TYPE_BALANCE_DISCUSSION_TIME_REMAINING = "BALANCE_DISCUSSION_TIME_REMAINING";
    public static final String TYPE_BALANCE_DISCUSSION_ENDED = "BALANCE_DISCUSSION_ENDED";

    // 기타
    public static final String TYPE_BALANCE_DISCUSSION_SKIPPED = "BALANCE_DISCUSSION_SKIPPED";
    public static final String TYPE_BALANCE_VOTING_STARTED = "BALANCE_VOTING_STARTED";
    public static final String TYPE_BALANCE_VOTING_PROGRESS = "BALANCE_VOTING_PROGRESS";
    public static final String TYPE_BALANCE_ROUND_COMPLETED = "BALANCE_ROUND_COMPLETED";
    public static final String TYPE_BALANCE_GAME_COMPLETED = "BALANCE_GAME_COMPLETED";
    public static final String TYPE_BALANCE_GAME_READY = "BALANCE_GAME_READY";

    private static final String BALANCE_ALL_MEMBERS_JOINED_MESSAGE = "모든 멤버가 입장했습니다. 게임을 시작합니다!";
    private static final String BALANCE_QUESTION_STARTED_FORMAT = "밸런스 문제를 확인해주세요. 남은 시간: %s";
    private static final String BALANCE_QUESTION_TIME_REMAINING_FORMAT = "남은 시간: %s";
    private static final String BALANCE_QUESTION_ENDED_MESSAGE = "문제 공개가 종료되었습니다. 토론을 시작해주세요.";
    private static final String BALANCE_DISCUSSION_STARTED_FORMAT = "토론 시간이 시작되었습니다. 남은 시간: %s";
    private static final String BALANCE_DISCUSSION_TIME_REMAINING_FORMAT = "남은 시간: %s";
    private static final String BALANCE_DISCUSSION_ENDED_MESSAGE = "토론 시간이 종료되었습니다. 투표를 시작해주세요.";
    private static final String BALANCE_DISCUSSION_SKIPPED_MESSAGE = "토론이 건너뛰어졌습니다. 투표를 시작해주세요.";
    private static final String BALANCE_VOTING_STARTED_MESSAGE = "투표가 시작되었습니다. 선택지를 골라주세요.";
    private static final String BALANCE_VOTING_PROGRESS_FORMAT = "투표 진행률: %d%%";
    private static final String BALANCE_ROUND_COMPLETED_FORMAT = "라운드 %d 투표가 완료되었습니다.";
    private static final String BALANCE_GAME_COMPLETED_MESSAGE = "모든 투표가 완료되었습니다. 결과를 확인해주세요.";
    private static final String BALANCE_GAME_READY_RESULT_FORMAT = "다른 참여자들의 결과 확인을 기다리고 있습니다... (%d/%d)";

    public BalanceChatMessage(String type, String nickname, String content, LocalDateTime timestamp, Object data) {
        super(type, nickname, content, timestamp, data);
    }

    public static ChatMessage enter(String nickname, int currentCount, int totalCount) {
        log.debug("밸런스 입장 메시지 생성: nickname={}, currentCount={}, totalCount={}", nickname, currentCount, totalCount);

        String content = String.format("%s님이 입장했습니다. 모든 멤버 입장을 기다리고 있습니다... (%d/%d)", nickname, currentCount, totalCount);
        ChatMessage message = of(TYPE_BALANCE_GAME_READY, SYSTEM_NICKNAME, content, 
                java.util.Map.of("currentCount", currentCount, "totalCount", totalCount, "memberName", nickname));

        log.debug("밸런스 입장 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage leave(String nickname, int currentCount, int totalCount) {
        log.debug("밸런스 퇴장 메시지 생성: nickname={}, currentCount={}, totalCount={}", nickname, currentCount, totalCount);

        String content = String.format("%s님이 퇴장했습니다. 모든 멤버 입장을 기다리고 있습니다... (%d/%d)", nickname, currentCount, totalCount);
        ChatMessage message = of(TYPE_BALANCE_GAME_READY, SYSTEM_NICKNAME, content, 
                java.util.Map.of("currentCount", currentCount, "totalCount", totalCount, "memberName", nickname));

        log.debug("밸런스 퇴장 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }



    public static ChatMessage balanceAllMembersJoined() {
        log.debug("밸런스 모든 멤버 입장 완료 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_ALL_MEMBERS_JOINED, SYSTEM_NICKNAME, BALANCE_ALL_MEMBERS_JOINED_MESSAGE);

        log.debug("밸런스 모든 멤버 입장 완료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceQuestionStarted(String remainingTime) {
        log.debug("밸런스 문제 공개 시작 메시지 생성: remainingTime={}", remainingTime);

        String content = String.format(BALANCE_QUESTION_STARTED_FORMAT, remainingTime);
        ChatMessage message = of(TYPE_BALANCE_QUESTION_STARTED, SYSTEM_NICKNAME, content, remainingTime);

        log.debug("밸런스 문제 공개 시작 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceQuestionTimeRemaining(String remainingTime) {
        log.debug("밸런스 문제 공개 시간 남음 메시지 생성: remainingTime={}", remainingTime);

        String content = String.format(BALANCE_QUESTION_TIME_REMAINING_FORMAT, remainingTime);
        ChatMessage message = of(TYPE_BALANCE_QUESTION_TIME_REMAINING, SYSTEM_NICKNAME, content, remainingTime);

        log.debug("밸런스 문제 공개 시간 남음 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceQuestionEnded() {
        log.debug("밸런스 문제 공개 종료 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_QUESTION_ENDED, SYSTEM_NICKNAME, BALANCE_QUESTION_ENDED_MESSAGE);

        log.debug("밸런스 문제 공개 종료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceDiscussionStarted(String remainingTime) {
        log.debug("밸런스 토론 시작 메시지 생성: remainingTime={}", remainingTime);

        String content = String.format(BALANCE_DISCUSSION_STARTED_FORMAT, remainingTime);
        ChatMessage message = of(TYPE_BALANCE_DISCUSSION_STARTED, SYSTEM_NICKNAME, content, remainingTime);

        log.debug("밸런스 토론 시작 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceDiscussionTimeRemaining(String remainingTime) {
        log.debug("밸런스 토론 시간 남음 메시지 생성: remainingTime={}", remainingTime);

        String content = String.format(BALANCE_DISCUSSION_TIME_REMAINING_FORMAT, remainingTime);
        ChatMessage message = of(TYPE_BALANCE_DISCUSSION_TIME_REMAINING, SYSTEM_NICKNAME, content, remainingTime);

        log.debug("밸런스 토론 시간 남음 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceDiscussionEnded() {
        log.debug("밸런스 토론 종료 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_DISCUSSION_ENDED, SYSTEM_NICKNAME, BALANCE_DISCUSSION_ENDED_MESSAGE);

        log.debug("밸런스 토론 종료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceDiscussionSkipped() {
        log.debug("밸런스 토론 건너뛰기 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_DISCUSSION_SKIPPED, SYSTEM_NICKNAME, BALANCE_DISCUSSION_SKIPPED_MESSAGE);

        log.debug("밸런스 토론 건너뛰기 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceVotingStarted() {
        log.debug("밸런스 투표 시작 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_VOTING_STARTED, SYSTEM_NICKNAME, BALANCE_VOTING_STARTED_MESSAGE);

        log.debug("밸런스 투표 시작 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceVotingProgress(int progress) {
        log.debug("밸런스 투표 진행률 메시지 생성: progress={}", progress);

        String content = String.format(BALANCE_VOTING_PROGRESS_FORMAT, progress);
        ChatMessage message = of(TYPE_BALANCE_VOTING_PROGRESS, SYSTEM_NICKNAME, content, progress);

        log.debug("밸런스 투표 진행률 메시지 생성 완료: type={}, content={}, progress={}",
                message.getType(), message.getContent(), progress);

        return message;
    }

    public static ChatMessage balanceRoundCompleted(int round) {
        log.debug("밸런스 라운드 완료 메시지 생성: round={}", round);

        String content = String.format(BALANCE_ROUND_COMPLETED_FORMAT, round + 1);
        ChatMessage message = of(TYPE_BALANCE_ROUND_COMPLETED, SYSTEM_NICKNAME, content, round);

        log.debug("밸런스 라운드 완료 메시지 생성 완료: type={}, content={}, round={}",
                message.getType(), message.getContent(), round);

        return message;
    }

    public static ChatMessage balanceGameCompleted() {
        log.debug("밸런스 게임 완료 메시지 생성");

        ChatMessage message = of(TYPE_BALANCE_GAME_COMPLETED, SYSTEM_NICKNAME, BALANCE_GAME_COMPLETED_MESSAGE);

        log.debug("밸런스 게임 완료 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }

    public static ChatMessage balanceGameReady(int currentCount, int totalCount) {
        log.debug("밸런스 게임 준비 메시지 생성: currentCount={}, totalCount={}", currentCount, totalCount);

        String content = String.format(BALANCE_GAME_READY_RESULT_FORMAT, currentCount, totalCount);
        ChatMessage message = of(TYPE_BALANCE_GAME_READY, SYSTEM_NICKNAME, content, 
                java.util.Map.of("currentCount", currentCount, "totalCount", totalCount));

        log.debug("밸런스 게임 준비 메시지 생성 완료: type={}, content={}",
                message.getType(), message.getContent());

        return message;
    }
} 