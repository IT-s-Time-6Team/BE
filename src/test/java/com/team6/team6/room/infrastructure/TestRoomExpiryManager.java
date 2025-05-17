package com.team6.team6.room.infrastructure;

import com.team6.team6.room.domain.RoomExpiryManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@Profile("test")
public class TestRoomExpiryManager implements RoomExpiryManager {
    // 메모리에만 저장하거나 아무 작업도 하지 않는 구현
    @Override
    public void scheduleExpiryWarning(String roomKey, Duration warningDelay) {
        // 로깅만 하고 실제로는 아무것도 하지 않음
        log.debug("테스트: 방 종료 경고 타이머 설정 (실행 안됨): {}", roomKey);
    }

    @Override
    public void scheduleRoomClosure(String roomKey, Duration closureDelay) {
        log.debug("테스트: 방 종료 타이머 설정 (실행 안됨): {}", roomKey);
    }

    @Override
    public void cancelAllTimers(String roomKey) {
        log.debug("테스트: 방 관련 모든 타이머 취소 (실행 안됨): {}", roomKey);
    }
}