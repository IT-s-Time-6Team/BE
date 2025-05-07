package com.team6.team6.room.domain;

import java.time.Duration;

public interface RoomExpiryManager {

    /**
     * 방 종료 5분 전 알림을 위한 타이머 설정
     */
    void scheduleExpiryWarning(String roomKey, Duration warningDelay);

    /**
     * 방 종료를 위한 타이머 설정
     */
    void scheduleRoomClosure(String roomKey, Duration closureDelay);

    /**
     * 방 관련 모든 타이머 취소
     */
    void cancelAllTimers(String roomKey);
}