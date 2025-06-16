package com.team6.team6.websocket.controller;

import com.team6.team6.keyword.dto.KeyEventRequest;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {


    @MessageMapping("/room/{roomKey}/key-event")
    @SendTo("/topic/room/{roomKey}/messages")
    public ChatMessage keyEvent(@Payload KeyEventRequest request, Principal principal) {

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        return ChatMessage.keyEvent(userPrincipal.getNickname(), request.key());
    }


    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(Exception exception) {
        return ChatMessage.error(exception);
    }
} 