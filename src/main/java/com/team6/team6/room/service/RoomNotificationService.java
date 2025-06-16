package com.team6.team6.room.service;

import com.team6.team6.keyword.dto.KeywordChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RoomNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendExpirationWarningNotification(String roomKey) {
        log.info("방 종료 5분 전 알림 발송: {}", roomKey);

        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", KeywordChatMessage.roomExpiryWarning());
    }

    public void sendClosedNotification(String roomKey) {

        log.info("방 종료 알림 발송: {}", roomKey);
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", KeywordChatMessage.roomExpired());
    }

    public void leaderRoomClosedNotification(String roomKey) {
        log.info("리더 방 종료 알림 발송: {}", roomKey);
        messagingTemplate.convertAndSend("/topic/room/" + roomKey + "/messages", KeywordChatMessage.leaderRoomExpired());
    }




}