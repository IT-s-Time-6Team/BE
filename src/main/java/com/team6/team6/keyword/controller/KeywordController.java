package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.dto.ChatMessage;
import com.team6.team6.keyword.dto.KeyEventRequest;
import com.team6.team6.keyword.dto.KeywordAddRequest;
import com.team6.team6.keyword.service.KeywordService;
import com.team6.team6.member.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;

    @MessageMapping("/room/{roomKey}/keyword")
    @SendToUser("/queue/keyword-confirmation")
    public ChatMessage addKeyword(@DestinationVariable String roomKey,
                                  @Payload KeywordAddRequest request,
                                  Principal principal) {

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        // 키워드 저장 및 분석 처리
        keywordService.addKeyword(request.toServiceRequest(roomKey, userPrincipal));

        return ChatMessage.keywordReceived(userPrincipal.getNickname(), request.keyword());
    }

    @MessageMapping("/room/{roomKey}/key-event")
    @SendTo("/topic/room/{roomKey}/messages")
    public ChatMessage keyEvent(@Payload KeyEventRequest request, Principal principal) {

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        return ChatMessage.keyEvent(userPrincipal.getNickname(), request.key());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(MessageConversionException exception) {

        return ChatMessage.error(exception);
    }
}
