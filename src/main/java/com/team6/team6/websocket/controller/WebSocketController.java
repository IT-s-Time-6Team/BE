package com.team6.team6.websocket.controller;

import com.team6.team6.keyword.dto.KeyEventRequest;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@Slf4j
public class WebSocketController {


    @MessageMapping("/room/{roomKey}/key-event")
    @SendTo("/topic/room/{roomKey}/messages")
    public ChatMessage keyEvent(@Payload KeyEventRequest request, Principal principal) {

        log.debug("키 이벤트 요청 수신: key={}", request.key());

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        log.debug("키 이벤트 처리 - 사용자 정보: memberId={}, nickname={}, roomKey={}, roomId={}, gameMode={}",
                userPrincipal.getId(), userPrincipal.getNickname(), userPrincipal.getRoomKey(),
                userPrincipal.getRoomId(), userPrincipal.getGameMode());

        ChatMessage keyEventMessage = ChatMessage.keyEvent(userPrincipal.getNickname(), request.key());

        log.debug("키 이벤트 메시지 생성 완료: type={}, nickname={}, content={}",
                keyEventMessage.getType(), keyEventMessage.getNickname(), keyEventMessage.getContent());

        return keyEventMessage;
    }


    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(Exception exception) {

        log.debug("웹소켓 메시지 처리 중 예외 발생: exceptionType={}, message={}",
                exception.getClass().getSimpleName(), exception.getMessage());

        if (log.isDebugEnabled()) {
            log.debug("예외 스택 트레이스:", exception);
        }

        ChatMessage errorMessage = ChatMessage.error(exception);

        log.debug("에러 메시지 생성 완료: type={}, content={}",
                errorMessage.getType(), errorMessage.getContent());

        return errorMessage;
    }
} 