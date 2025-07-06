package com.team6.team6.balance.infrastructure;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.dto.BalanceChatMessage;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompBalanceMessagePublisher implements BalanceMessagePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_PREFIX = "/topic/room/";
    private static final String TOPIC_SUFFIX = "/messages";



    @Override
    public void notifyBalanceAllMembersJoined(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceAllMembersJoined();
        sendMessage(roomKey, message);
        log.debug("밸런스 모든 멤버 입장 완료 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceQuestionStarted(String roomKey, String remainingTime) {
        ChatMessage message = BalanceChatMessage.balanceQuestionStarted(remainingTime);
        sendMessage(roomKey, message);
        log.debug("밸런스 문제 공개 시작 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceQuestionTimeRemaining(String roomKey, String remainingTime) {
        ChatMessage message = BalanceChatMessage.balanceQuestionTimeRemaining(remainingTime);
        sendMessage(roomKey, message);
        log.debug("밸런스 문제 공개 시간 알림 발송: {}, 남은시간: {}", roomKey, remainingTime);
    }

    @Override
    public void notifyBalanceQuestionEnded(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceQuestionEnded();
        sendMessage(roomKey, message);
        log.debug("밸런스 문제 공개 종료 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceDiscussionStarted(String roomKey, String remainingTime) {
        ChatMessage message = BalanceChatMessage.balanceDiscussionStarted(remainingTime);
        sendMessage(roomKey, message);
        log.debug("밸런스 토론 시작 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceDiscussionTimeRemaining(String roomKey, String remainingTime) {
        ChatMessage message = BalanceChatMessage.balanceDiscussionTimeRemaining(remainingTime);
        sendMessage(roomKey, message);
        log.debug("밸런스 토론 시간 알림 발송: {}, 남은시간: {}", roomKey, remainingTime);
    }

    @Override
    public void notifyBalanceDiscussionEnded(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceDiscussionEnded();
        sendMessage(roomKey, message);
        log.debug("밸런스 토론 종료 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceDiscussionSkipped(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceDiscussionSkipped();
        sendMessage(roomKey, message);
        log.debug("밸런스 토론 건너뛰기 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceVotingStarted(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceVotingStarted();
        sendMessage(roomKey, message);
        log.debug("밸런스 투표 시작 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceVotingProgress(String roomKey, int progress) {
        ChatMessage message = BalanceChatMessage.balanceVotingProgress(progress);
        sendMessage(roomKey, message);
        log.debug("밸런스 투표 진행률 알림 발송: {}, 진행률: {}%", roomKey, progress);
    }

    @Override
    public void notifyBalanceRoundCompleted(String roomKey, int round) {
        ChatMessage message = BalanceChatMessage.balanceRoundCompleted(round);
        sendMessage(roomKey, message);
        log.debug("밸런스 라운드 완료 알림 발송: {}, 라운드: {}", roomKey, round + 1);
    }

    @Override
    public void notifyBalanceGameCompleted(String roomKey) {
        ChatMessage message = BalanceChatMessage.balanceGameCompleted();
        sendMessage(roomKey, message);
        log.debug("밸런스 게임 완료 알림 발송: {}", roomKey);
    }

    @Override
    public void notifyBalanceGameReady(String roomKey, int currentCount, int totalCount) {
        ChatMessage message = BalanceChatMessage.balanceGameReady(currentCount, totalCount);
        sendMessage(roomKey, message);
        log.debug("밸런스 게임 준비 알림 발송: {}, 현재/전체: {}/{}", roomKey, currentCount, totalCount);
    }

    private void sendMessage(String roomKey, ChatMessage message) {
        String destination = TOPIC_PREFIX + roomKey + TOPIC_SUFFIX;
        messagingTemplate.convertAndSend(destination, message);
    }
} 