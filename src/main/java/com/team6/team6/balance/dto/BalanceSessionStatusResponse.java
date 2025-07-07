package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceGameStep;
import lombok.Builder;

@Builder
public record BalanceSessionStatusResponse(
        BalanceGameStep currentStep,    // 현재 게임 단계
        boolean hasUserSubmitted,       // 유저가 투표했는지 여부 (투표 단계일 때만)
        boolean waitingForOthers,       // 다른 사람들을 기다리고 있는지 여부 (결과 확인 단계일 때만)
        int progress,                   // 진행률 (투표 진행률 또는 결과 확인 진행률)
        int currentRound,               // 현재 라운드 (1부터 시작)
        int totalRounds                 // 총 라운드 수
) {
    
    public static BalanceSessionStatusResponse of(BalanceGameStep currentStep, boolean hasUserSubmitted,
                                                boolean waitingForOthers, int progress, int currentRound, int totalRounds) {
        return BalanceSessionStatusResponse.builder()
                .currentStep(currentStep)
                .hasUserSubmitted(hasUserSubmitted)
                .waitingForOthers(waitingForOthers)
                .progress(progress)
                .currentRound(currentRound + 1) // 사용자에게는 1부터 시작
                .totalRounds(totalRounds)
                .build();
    }
} 