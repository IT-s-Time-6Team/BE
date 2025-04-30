package com.team6.team6.keyword.controller;

import com.team6.team6.keyword.dto.ChatMessage;
import com.team6.team6.keyword.dto.KeywordAddServiceReq;
import com.team6.team6.keyword.service.KeywordService;
import com.team6.team6.member.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class KeywordController {

    private final KeywordService keywordService;
    private final SimpMessageSendingOperations messagingTemplate;

//    // 사용자 방 입장 기록을 위한 맵 (방ID -> {멤버ID -> 입장 여부})
//    private final Map<Long, Map<Long, Boolean>> roomMemberRegistry = new ConcurrentHashMap<>();

    @MessageMapping("/room/{roomKey}/keyword")
    @SendToUser("/queue/keyword-confirmation")
    public ChatMessage addKeyword(@DestinationVariable String roomKey,
                           @Payload Map<String, String> payload,
                           Authentication authentication) {
        // payload에서 키워드 추출
        String keyword = payload.get("keyword");

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long memberId = principal.getId();
        Long roomId = principal.getRoomId();
        String nickname = principal.getNickname();

        // 키워드 저장 및 분석 처리
        KeywordAddServiceReq req = KeywordAddServiceReq.of(keyword, roomKey, roomId, memberId);

        keywordService.addKeyword(req);
        return ChatMessage.keywordReceived(nickname, keyword);
    }

//    @MessageMapping("/room/{roomKey}/enter")
//    public void enterRoom(@DestinationVariable String roomKey, Authentication authentication) {
//        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
//        Long memberId = principal.getId();
//        String nickname = principal.getNickname();
//        Long roomId = principal.getRoomId();
//
//        // 방에 대한 멤버 레지스트리 조회 또는 생성
//        Map<Long, Boolean> memberRegistry = roomMemberRegistry.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());
//
//        // 채팅 메시지 생성 및 전송
//        ChatMessage message;
//        if (memberRegistry.containsKey(memberId)) {
//            message = ChatMessage.reenter(nickname);
//        } else {
//            message = ChatMessage.enter(nickname);
//        }
//
//        // 메시지 전송
//        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", message);
//
//        // 사용자 입장 기록
//        memberRegistry.put(memberId, true);
//    }
}