package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
        @NotNull(message = "공감 기준 인원을 입력해주세요")
        @Min(value = 2, message = "공감 기준 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 20, message = "공감 기준 인원은 최대 20명입니다")
        Integer requiredAgreements,

        @NotNull(message = "최대 입장 인원을 입력해주세요")
        @Min(value = 2, message = "최대 입장 인원은 최소 2명 이상이어야 합니다")
        @Max(value = 20, message = "최대 입장 인원은 최대 20명입니다")
        Integer maxMember,

        @Min(value = 5, message = "시간 제한은 최소 5분 이상이어야 합니다")
        @Max(value = 360, message = "시간 제한은 최대 360분(6시간)입니다")
        Integer durationMinutes,

        @NotNull(message = "게임 모드를 선택해주세요")
        GameMode gameMode
) {

    public RoomCreateServiceRequest toServiceRequest() {
        return RoomCreateServiceRequest.builder()
                .requiredAgreements(requiredAgreements)
                .maxMember(maxMember)
                .durationMinutes(setDefaultDurationMinutes(durationMinutes))
                .gameMode(gameMode)
                .build();
    }

    private Integer setDefaultDurationMinutes(Integer durationMinutes) {
        return durationMinutes == null ? 30 : durationMinutes;
    }
}