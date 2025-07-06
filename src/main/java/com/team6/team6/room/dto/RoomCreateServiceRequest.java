package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import lombok.Builder;

public record RoomCreateServiceRequest(
        Integer requiredAgreements,
        Integer maxMember,
        Integer durationMinutes,
        GameMode gameMode,
        Integer balanceQuestionCount
) {
    @Builder
    public RoomCreateServiceRequest {
    }


}