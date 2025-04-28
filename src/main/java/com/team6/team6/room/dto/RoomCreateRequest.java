package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record RoomCreateRequest(
        @NotNull(message = "공감 기준 인원을 입력해주세요")
        @Min(value = 2, message = "공감 기준 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 20, message = "공감 기준 인원은 최대 20명입니다")
        Integer requiredAgreements,

        @NotNull(message = "최대 입장 인원을 입력해주세요")
        @Min(value = 2, message = "최대 입장 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 20, message = "최대 입장 인원은 최대 20명입니다")
        Integer maxMember,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime timeLimit,

        @NotNull(message = "게임 모드를 선택해주세요")
        GameMode gameMode
) {

    public RoomCreateServiceRequest toServiceRequest() {
        return RoomCreateServiceRequest.builder()
                .requiredAgreements(requiredAgreements)
                .maxMember(maxMember)
                .timeLimit(setDefaultTimeLimit(timeLimit))
                .gameMode(gameMode)
                .build();
    }

    private LocalDateTime setDefaultTimeLimit(LocalDateTime timeLimit) {
        return timeLimit == null ? LocalDateTime.now().plusHours(6) : timeLimit;
    }
}