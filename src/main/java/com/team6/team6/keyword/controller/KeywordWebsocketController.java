package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.dto.KeywordAddRequest;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.service.KeywordService;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class KeywordWebsocketController {

    private final KeywordService keywordService;

    @MessageMapping("/room/{roomKey}/keyword")
    @SendToUser("/queue/keyword-confirmation")
    public ChatMessage addKeyword(@DestinationVariable String roomKey,
                                  @Payload @Valid KeywordAddRequest request,
                                  Principal principal) {

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        // 키워드 저장 및 분석 처리
        keywordService.addKeyword(request.toServiceRequest(roomKey, userPrincipal));

        return KeywordChatMessage.keywordReceived(userPrincipal.getNickname(), request.keyword());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(Exception exception) {

        return KeywordChatMessage.keywordError(exception);
    }
}
