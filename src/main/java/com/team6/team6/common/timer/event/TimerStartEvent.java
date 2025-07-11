package com.team6.team6.common.timer.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TimerStartEvent {
    private final String timerKey;
    private final String roomKey;
    private final Long roomId;
    private final long initialSeconds;
    private final String formattedTime;
    private final String timerType;
} 