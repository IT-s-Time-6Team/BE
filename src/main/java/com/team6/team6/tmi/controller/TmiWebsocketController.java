package com.team6.team6.tmi.controller;

import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.tmi.dto.TmiChatMessage;
import com.team6.team6.tmi.dto.TmiSubmitRequest;
import com.team6.team6.tmi.service.TmiSubmitService;
import com.team6.team6.websocket.dto.ChatMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TmiWebsocketController {

    private final TmiSubmitService tmiSubmitService;

    @MessageMapping("/room/{roomKey}/tmi/submit")
    @SendToUser("/queue/tmi-confirmation")
    public ChatMessage submitTmi(@DestinationVariable String roomKey,
                                 @Payload @Valid TmiSubmitRequest request,
                                 Principal principal) {

        log.debug("TMI 제출 요청 수신: roomKey={}, tmi={}", roomKey, request.tmiContent());

        UserPrincipal userPrincipal = (UserPrincipal) ((Authentication) principal).getPrincipal();

        log.debug("사용자 정보 추출: memberId={}, nickname={}, roomId={}",
                userPrincipal.getId(), userPrincipal.getNickname(), userPrincipal.getRoomId());

        // TMI 제출 처리
        tmiSubmitService.submitTmi(request.toServiceRequest(userPrincipal));

        log.debug("TMI 서비스 처리 완료: roomKey={}, memberName={}, content={}",
                roomKey, userPrincipal.getNickname(), request.tmiContent());

        ChatMessage responseMessage = TmiChatMessage.tmiReceived(
                userPrincipal.getNickname(),
                request.tmiContent()
        );

        log.debug("TMI 수신 확인 메시지 생성: type={}, nickname={}, content={}",
                responseMessage.getType(), responseMessage.getNickname(), responseMessage.getContent());

        return responseMessage;
    }
}
