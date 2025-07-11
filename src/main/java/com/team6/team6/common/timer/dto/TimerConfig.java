package com.team6.team6.common.timer.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimerConfig {
    private final String timerKey;
    private final String roomKey;
    private final Long roomId;
    private final long durationSeconds;
    private final String timerType;

    public static TimerConfig of(String timerKey, String roomKey, Long roomId,
                                 long durationSeconds, String timerType) {
        return TimerConfig.builder()
                .timerKey(timerKey)
                .roomKey(roomKey)
                .roomId(roomId)
                .durationSeconds(durationSeconds)
                .timerType(timerType)
                .build();
    }
} 