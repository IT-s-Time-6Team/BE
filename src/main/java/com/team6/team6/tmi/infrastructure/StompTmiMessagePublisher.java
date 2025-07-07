package com.team6.team6.tmi.infrastructure;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.dto.TmiChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompTmiMessagePublisher implements TmiMessagePublisher {

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void notifyTmiCollectionProgress(String roomKey, int progress) {
        log.debug("TMI 수집 진행률 브로드캐스트: roomKey={}, progress={}", roomKey, progress);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiCollectionProgress(progress));

        log.debug("TMI 수집 진행률 브로드캐스트 완료: destination={}, progress={}", destination, progress);
    }

    @Override
    public void notifyTmiCollectionCompleted(String roomKey) {
        log.debug("TMI 수집 완료 브로드캐스트: roomKey={}", roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiCollectionCompleted());

        log.debug("TMI 수집 완료 브로드캐스트 완료: destination={}", destination);
    }

    @Override
    public void notifyTmiVotingStarted(String roomKey) {
        log.debug("TMI 투표 시작 브로드캐스트: roomKey={}",
                roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiVotingStarted());

        log.debug("TMI 투표 시작 브로드캐스트 완료: destination={}",
                destination);
    }

    @Override
    public void notifyTmiRoundCompleted(String roomKey, int round) {
        log.debug("TMI 라운드 완료 브로드캐스트: roomKey={}, round={}", roomKey, round);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiRoundVotingCompleted(round));

        log.debug("TMI 라운드 완료 브로드캐스트 완료: destination={}, round={}", destination, round);
    }

    @Override
    public void notifyTmiVotingProgress(String roomKey, int progress) {
        log.debug("TMI 투표 진행률 브로드캐스트: roomKey={}, progress={}", roomKey, progress);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiVotingProgress(progress));

        log.debug("TMI 투표 진행률 브로드캐스트 완료: destination={}, progress={}", destination, progress);
    }

    @Override
    public void notifyTmiAllVotingCompleted(String roomKey) {
        log.debug("TMI 전체 투표 완료 브로드캐스트: roomKey={}", roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiAllVotingCompleted());

        log.debug("TMI 전체 투표 완료 브로드캐스트 완료: destination={}", destination);
    }

    @Override
    public void notifyTmiHintStarted(String roomKey, String remainingTime) {
        log.debug("TMI 힌트 시작 브로드캐스트: roomKey={}, remainingTime={}", roomKey, remainingTime);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiHintStarted(remainingTime));

        log.debug("TMI 힌트 시작 브로드캐스트 완료: destination={}, remainingTime={}", destination, remainingTime);
    }

    @Override
    public void notifyTmiHintTimeRemaining(String roomKey, String remainingTime) {
        log.debug("TMI 힌트 타임 남은 시간 브로드캐스트: roomKey={}, remainingTime={}", roomKey, remainingTime);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiHintTimeRemaining(remainingTime));

        log.debug("TMI 힌트 타임 남은 시간 브로드캐스트 완료: destination={}, remainingTime={}", destination, remainingTime);
    }

    @Override
    public void notifyTmiHintEnded(String roomKey) {
        log.debug("TMI 힌트 종료 브로드캐스트: roomKey={}", roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiHintEnded());

        log.debug("TMI 힌트 종료 브로드캐스트 완료: destination={}", destination);
    }

    @Override
    public void notifyTmiHintSkipped(String roomKey) {
        log.debug("TMI 힌트 건너뛰기 브로드캐스트: roomKey={}", roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiHintEnded());

        log.debug("TMI 힌트 건너뛰기 브로드캐스트 완료: destination={}", destination);
    }
}
