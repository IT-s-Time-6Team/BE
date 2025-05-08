package com.team6.team6.room.service;

import com.team6.team6.room.domain.RoomExpiryManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RoomExpiryService {

    private final RoomExpiryManager roomExpiryManager;

    public void scheduleRoomExpiry(String roomKey, int durationMinutes) {
        // 방 종료 5분 전 알림 타이머 설정
        if (durationMinutes > 5) {
            roomExpiryManager.scheduleExpiryWarning(
                    roomKey,
                    Duration.ofMinutes(durationMinutes - 5)
            );
        }

        // 방 종료 타이머 설정
        roomExpiryManager.scheduleRoomClosure(
                roomKey,
                Duration.ofMinutes(durationMinutes)
        );
    }

    public void cancelRoomExpiry(String roomKey) {
        roomExpiryManager.cancelAllTimers(roomKey);
    }
}