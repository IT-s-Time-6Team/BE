package com.team6.team6.room.service;

import com.team6.team6.keyword.dto.ChatMessage;
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
                        m.type() == ChatMessage.MessageType.ROOM_EXPIRED &&
                                m.nickname().equals("@시스템") &&
                                m.content().equals("방이 종료되었습니다.")
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
                        m.type() == ChatMessage.MessageType.ROOM_EXPIRY_WARNING &&
                                m.nickname().equals("@시스템") &&
                                m.content().equals("방 종료까지 5분 남았습니다.")
                )
        );
    }
}