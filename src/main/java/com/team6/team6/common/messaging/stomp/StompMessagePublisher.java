package com.team6.team6.common.messaging.stomp;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompMessagePublisher implements MessagePublisher {

    private final SimpMessageSendingOperations messagingTemplate;

    @Override
    public void publishKeywordAnalysisResult(String roomKey, Object results) {
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", ChatMessage.analysisResult(results));
    }
}