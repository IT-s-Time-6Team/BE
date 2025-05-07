package com.team6.team6.room.controller;

import com.team6.team6.room.service.RoomNotificationService;
import com.team6.team6.room.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private static final String ROOM_WARNING_PREFIX = "room_warning:";
    private static final String ROOM_CLOSE_PREFIX = "room_close:";
    private final RoomNotificationService roomNotificationService;
    private final RoomService roomService;

    public RedisKeyExpirationListener(
            RedisMessageListenerContainer listenerContainer,
            RoomNotificationService roomNotificationService,
            RoomService roomService) {
        super(listenerContainer);
        this.roomNotificationService = roomNotificationService;
        this.roomService = roomService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("만료된 키: {}", expiredKey);

        if (expiredKey.startsWith(ROOM_WARNING_PREFIX)) {
            String roomKey = expiredKey.substring(ROOM_WARNING_PREFIX.length());
            roomNotificationService.sendExpirationWarningNotification(roomKey);
        } else if (expiredKey.startsWith(ROOM_CLOSE_PREFIX)) {
            String roomKey = expiredKey.substring(ROOM_CLOSE_PREFIX.length());
            roomNotificationService.sendClosedNotification(roomKey);
            roomService.closeRoom(roomKey);
        }
    }
}