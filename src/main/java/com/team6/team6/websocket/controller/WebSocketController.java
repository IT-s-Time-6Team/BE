package com.team6.team6.websocket.controller;

import com.team6.team6.websocket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(Exception exception) {
        return ChatMessage.error(exception);
    }
} 