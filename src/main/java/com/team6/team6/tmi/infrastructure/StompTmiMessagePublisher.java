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
    public void publishTmiCollectionProgress(String roomKey, int progress) {
        log.debug("TMI 수집 진행률 브로드캐스트: roomKey={}, progress={}", roomKey, progress);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiCollectionProgress(progress));

        log.debug("TMI 수집 진행률 브로드캐스트 완료: destination={}, progress={}", destination, progress);
    }

    @Override
    public void publishTmiCollectionCompleted(String roomKey) {
        log.debug("TMI 수집 완료 브로드캐스트: roomKey={}", roomKey);

        String destination = "/topic/room/" + roomKey + "/messages";
        messagingTemplate.convertAndSend(destination, TmiChatMessage.tmiCollectionCompleted());

        log.debug("TMI 수집 완료 브로드캐스트 완료: destination={}", destination);
    }
}
