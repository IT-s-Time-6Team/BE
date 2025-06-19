package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.dto.KeywordAddRequest;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.service.KeywordService;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.dto.ChatMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class KeywordWebsocketController {

    private final KeywordService keywordService;

    @MessageMapping("/room/{roomKey}/keyword")
    @SendToUser("/queue/keyword-confirmation")
    public ChatMessage addKeyword(@DestinationVariable String roomKey,
                                  @Payload @Valid KeywordAddRequest request,
                                  Principal principal) {

        log.debug("키워드 추가 요청 수신: roomKey={}, keyword={}", roomKey, request.keyword());

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        log.debug("사용자 정보 추출: memberId={}, nickname={}, roomId={}, gameMode={}",
                userPrincipal.getId(), userPrincipal.getNickname(),
                userPrincipal.getRoomId(), userPrincipal.getGameMode());

        // 키워드 저장 및 분석 처리
        keywordService.addKeyword(request.toServiceRequest(roomKey, userPrincipal));

        log.debug("키워드 서비스 처리 완료: roomKey={}, keyword={}, memberId={}",
                roomKey, request.keyword(), userPrincipal.getId());

        ChatMessage responseMessage = KeywordChatMessage.keywordReceived(userPrincipal.getNickname(), request.keyword());

        log.debug("키워드 수신 확인 메시지 생성: type={}, nickname={}, content={}",
                responseMessage.getType(), responseMessage.getNickname(), responseMessage.getContent());

        return responseMessage;
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public ChatMessage handleException(Exception exception) {

        log.debug("키워드 처리 중 예외 발생: exceptionType={}, message={}",
                exception.getClass().getSimpleName(), exception.getMessage());

        if (log.isDebugEnabled()) {
            log.debug("예외 스택 트레이스:", exception);
        }

        ChatMessage errorMessage = KeywordChatMessage.keywordError(exception);

        log.debug("에러 메시지 생성: type={}, content={}",
                errorMessage.getType(), errorMessage.getContent());

        return errorMessage;
    }
}
