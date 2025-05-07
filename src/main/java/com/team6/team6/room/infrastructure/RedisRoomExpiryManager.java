package com.team6.team6.room.infrastructure;

import com.team6.team6.room.domain.RoomExpiryManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisRoomExpiryManager implements RoomExpiryManager {

    private static final String ROOM_WARNING_PREFIX = "room_warning:";
    private static final String ROOM_CLOSE_PREFIX = "room_close:";
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void scheduleExpiryWarning(String roomKey, Duration warningDelay) {
        log.info("방 종료 경고 타이머 설정: {}, 지연시간: {}", roomKey, warningDelay);
        redisTemplate.opsForValue().set(
                ROOM_WARNING_PREFIX + roomKey,
                roomKey,
                warningDelay
        );
    }

    @Override
    public void scheduleRoomClosure(String roomKey, Duration closureDelay) {
        log.info("방 종료 타이머 설정: {}, 지연시간: {}", roomKey, closureDelay);
        redisTemplate.opsForValue().set(
                ROOM_CLOSE_PREFIX + roomKey,
                roomKey,
                closureDelay
        );
    }

    @Override
    public void cancelAllTimers(String roomKey) {
        log.info("방 관련 모든 타이머 취소: {}", roomKey);
        redisTemplate.delete(ROOM_WARNING_PREFIX + roomKey);
        redisTemplate.delete(ROOM_CLOSE_PREFIX + roomKey);
    }
}