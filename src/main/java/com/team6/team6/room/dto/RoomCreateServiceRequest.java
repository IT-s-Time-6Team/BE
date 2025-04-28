package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import lombok.Builder;

import java.time.LocalDateTime;

public record RoomCreateServiceRequest(
        Integer requiredAgreements,
        Integer maxMember,
        LocalDateTime timeLimit,
        GameMode gameMode
) {
    @Builder
    public RoomCreateServiceRequest {}


}