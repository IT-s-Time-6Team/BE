package com.team6.team6.common.messaging.stomp;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

/**
 * STOMP 메시지 발행을 위한 임시 구현체
 * <p>
 * 추가 구현 필요
 * </p>
 */
@Component
@RequiredArgsConstructor
public class StompMessagePublisher implements MessagePublisher {

//    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void publishKeywordAnalysisResult(Long roomId, Object results) {
//        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/keyword", results);
    }
}