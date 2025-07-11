package com.team6.team6.room.service;

import com.team6.team6.websocket.dto.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
class RoomNotificationServiceTest {

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomNotificationService roomNotificationService;

    @Test
    void 방_종료_알림_메시지_전송() {
        // given
        String roomKey = "room789";

        // when
        roomNotificationService.sendClosedNotification(roomKey);

        // then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/room/" + roomKey + "/messages"),
                argThat((ChatMessage m) ->
                        "ROOM_EXPIRED".equals(m.getType()) &&
                                "@시스템".equals(m.getNickname()) &&
                                "방이 종료되었습니다.".equals(m.getContent())
                )
        );
    }

    @Test
    void 방_종료_경고_알림_메시지_전송() {
        // given
        String roomKey = "room999";

        // when
        roomNotificationService.sendExpirationWarningNotification(roomKey);

        // then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/room/" + roomKey + "/messages"),
                argThat((ChatMessage m) ->
                        "ROOM_EXPIRY_WARNING".equals(m.getType()) &&
                                "@시스템".equals(m.getNickname()) &&
                                "방 종료까지 5분 남았습니다.".equals(m.getContent())
                )
        );
    }
}