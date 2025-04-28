package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import lombok.Builder;

import java.time.LocalDateTime;

public record RoomResponse(
        String roomKey,
        Integer requiredAgreements,
        Integer maxMember,
        LocalDateTime timeLimit,
        GameMode gameMode,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        Boolean isClosed
) {
    @Builder
    public RoomResponse {}

    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .roomKey(room.getRoomKey())
                .requiredAgreements(room.getRequiredAgreements())
                .maxMember(room.getMaxMember())
                .timeLimit(room.getTimeLimit())
                .gameMode(room.getGameMode())
                .createdAt(room.getCreatedAt())
                .closedAt(room.getClosedAt())
                .isClosed(room.getClosedAt() != null)
                .build();
    }
}