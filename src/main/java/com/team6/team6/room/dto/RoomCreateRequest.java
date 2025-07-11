package com.team6.team6.room.dto;

import com.team6.team6.room.entity.GameMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RoomCreateRequest(
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
        GameMode gameMode,

        @Min(value = 1, message = "밸런스 문제 개수는 최소 1개 이상이어야 합니다")
        @Max(value = 7, message = "밸런스 문제 개수는 최대 7개입니다")
        Integer balanceQuestionCount    // Balance 모드 전용
) {

    public RoomCreateServiceRequest toServiceRequest() {
        return new RoomCreateServiceRequest(
                setRequiredAgreements(),
                maxMember,
                setDefaultDurationMinutes(setDurationMinutes()),
                gameMode,
                setBalanceQuestionCount()
        );
    }

    private Integer setRequiredAgreements() {
        if (gameMode == GameMode.TMI || gameMode == GameMode.BALANCE) {
            return null; // TMI, BALANCE 모드에서는 공감 기준 불필요
        }
        return requiredAgreements;
    }

    private Integer setDurationMinutes() {
        if (gameMode == GameMode.TMI || gameMode == GameMode.BALANCE) {
            return 24 * 60; // TMI, BALANCE 모드는 24시간 (1440분)
        }
        return durationMinutes == null ? 30 : durationMinutes;
    }

    private Integer setDefaultDurationMinutes(Integer durationMinutes) {
        return durationMinutes == null ? 30 : durationMinutes;
    }

    private Integer setBalanceQuestionCount() {
        if (gameMode == GameMode.BALANCE) {
            return balanceQuestionCount == null ? 5 : balanceQuestionCount;
        }
        return null;
    }
}
