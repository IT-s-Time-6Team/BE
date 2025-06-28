package com.team6.team6.tmi.dto;

import com.team6.team6.tmi.entity.TmiGameStep;
import lombok.Builder;

@Builder
public record TmiSessionStatusResponse(
    TmiGameStep currentStep,    // 현재 게임 단계
    boolean hasUserSubmitted,    // 유저가 제출/투표 했는지 여부
    int progress
) {}
