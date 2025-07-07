package com.team6.team6.balance.listener;

import com.team6.team6.balance.service.BalanceWebSocketSubscribeService;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
import com.team6.team6.websocket.event.WebSocketDisconnectEvent;
import com.team6.team6.websocket.util.WebSocketUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Balance 모드 전용 웹소켓 이벤트 리스너
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceWebSocketEventListener {

    private static final Pattern ROOM_TOPIC_PATTERN = Pattern.compile("/topic/room/([^/]+)/messages");

    private final SimpMessageSendingOperations messagingTemplate;
    private final BalanceWebSocketSubscribeService balanceWebSocketSubscribeService;
    private final RoomMemberStateManager roomMemberStateManager;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        log.debug("Balance 모드 구독 이벤트 수신: sessionId={}", event.getMessage().getHeaders().get("simpSessionId"));

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        log.debug("구독 대상 destination: {}", destination);

        // 대상 destination과 방 키 추출
        String roomKey = extractRoomKeyIfRoomSubscription(destination);
        if (roomKey == null) {
            log.debug("방 구독이 아니므로 처리하지 않음: destination={}", destination);
            return;
        }

        log.debug("방 키 추출 성공: roomKey={}", roomKey);

        // 사용자 인증 정보 추출
        UserPrincipal principal = WebSocketUtil.extractUserPrincipalFromStompHeader(event);

        log.debug("사용자 인증 정보 추출: memberId={}, nickname={}, roomId={}, gameMode={}",
                principal.getId(), principal.getNickname(), principal.getRoomId(), principal.getGameMode());

        // BALANCE 게임 모드가 아니면 처리하지 않음
        if (!"BALANCE".equals(principal.getGameMode())) {
            log.debug("BALANCE 게임 모드가 아니므로 처리하지 않음: gameMode={}", principal.getGameMode());
            return;
        }

        String nickname = principal.getNickname();
        Long roomId = principal.getRoomId();
        Long memberId = principal.getId();

        log.debug("Balance 모드 구독 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        // 서비스에 사용자 구독 처리 위임
        ChatMessage message = balanceWebSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        if (message != null) {
            log.debug("구독 처리 메시지 생성: type={}, nickname={}, content={}",
                    message.getType(), message.getNickname(), message.getContent());

            // 메시지 전송
            String topicDestination = "/topic/room/" + roomKey + "/messages";
            messagingTemplate.convertAndSend(topicDestination, message);

            log.debug("구독 처리 메시지 전송 완료: destination={}", topicDestination);
        }

        log.info("Balance 모드 구독 처리: roomKey={}, nickname={}", roomKey, nickname);
    }

    @EventListener
    public void handleDisconnect(WebSocketDisconnectEvent event) {
        log.debug("Balance 모드 연결 해제 이벤트 수신: gameMode={}", event.getGameMode());

        // BALANCE 게임 모드가 아니면 처리하지 않음
        if (!"BALANCE".equals(event.getGameMode())) {
            log.debug("BALANCE 게임 모드가 아니므로 처리하지 않음: gameMode={}", event.getGameMode());
            return;
        }

        UserPrincipal principal = event.getPrincipal();
        String roomKey = event.getRoomKey();
        String nickname = principal.getNickname();
        Long roomId = principal.getRoomId();

        log.debug("Balance 모드 연결 해제 처리 시작: roomKey={}, nickname={}, memberId={}, roomId={}",
                roomKey, nickname, principal.getId(), roomId);

        // 서비스에 연결 해제 처리 위임
        ChatMessage leaveMessage = balanceWebSocketSubscribeService.handleUserDisconnection(roomKey, nickname, roomId);

        if (leaveMessage != null) {
            log.debug("연결 해제 메시지 생성: type={}, nickname={}, content={}",
                    leaveMessage.getType(), leaveMessage.getNickname(), leaveMessage.getContent());

            // 메시지 전송
            String topicDestination = "/topic/room/" + roomKey + "/messages";
            messagingTemplate.convertAndSend(topicDestination, leaveMessage);

            log.debug("연결 해제 메시지 전송 완료: destination={}", topicDestination);
        }

        log.info("Balance 모드 연결 해제 처리: roomKey={}, nickname={}", roomKey, nickname);
    }

    /**
     * 대상 URL이 채팅방 구독 요청인지 확인하고, 방 키를 추출
     */
    private String extractRoomKeyIfRoomSubscription(String destination) {
        if (destination == null) {
            log.debug("destination이 null이므로 방 키 추출 불가");
            return null;
        }

        Matcher matcher = ROOM_TOPIC_PATTERN.matcher(destination);
        if (matcher.matches()) {
            String roomKey = matcher.group(1);
            log.debug("방 키 추출 성공: destination={}, roomKey={}", destination, roomKey);
            return roomKey;
        } else {
            log.debug("방 구독 패턴과 일치하지 않음: destination={}", destination);
            return null;
        }
    }
} 