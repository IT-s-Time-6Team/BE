package com.team6.team6.room.controller;

import com.team6.team6.room.service.RoomNotificationService;
import com.team6.team6.room.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@SpringBootTest
class RedisKeyExpirationListenerTest {

    @MockitoBean
    private RoomNotificationService roomNotificationService;

    @MockitoBean
    private RoomService roomService;

    @MockitoBean
    private RedisMessageListenerContainer listenerContainer;

    @Test
    void redis_키_만료_이벤트_리스너_경고_동작() {
        // given
        String roomKey = "room123";
        String expiredKey = "room_warning:" + roomKey;
        Message message = mock(Message.class);
        when(message.toString()).thenReturn(expiredKey);

        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(
                listenerContainer, roomNotificationService, roomService
        );

        // when
        listener.onMessage(message, null);

        // then
        verify(roomNotificationService).sendExpirationWarningNotification(roomKey);
        verify(roomNotificationService, never()).sendClosedNotification(anyString());
        verify(roomService, never()).closeRoom(anyString());
    }

    @Test
    void redis_키_만료_이벤트_리스너_종료_동작() {
        // given
        String roomKey = "room456";
        String expiredKey = "room_close:" + roomKey;
        Message message = mock(Message.class);
        when(message.toString()).thenReturn(expiredKey);

        RedisKeyExpirationListener listener = new RedisKeyExpirationListener(
                listenerContainer, roomNotificationService, roomService
        );

        // when
        listener.onMessage(message, null);

        // then
        verify(roomNotificationService).sendClosedNotification(roomKey);
        verify(roomService).closeRoom(roomKey);
        verify(roomNotificationService, never()).sendExpirationWarningNotification(anyString());
    }
}