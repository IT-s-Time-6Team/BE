package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceSessionQuestion;
import lombok.Builder;

@Builder
public record BalanceQuestionResponse(
        String questionA,
        String questionB,
        int currentRound,
        int totalRounds
) {
    
    public static BalanceQuestionResponse of(String questionA, String questionB, 
                                           int currentRound, int totalRounds) {
        return BalanceQuestionResponse.builder()
                .questionA(questionA)
                .questionB(questionB)
                .currentRound(currentRound + 1) // 사용자에게는 1부터 시작하도록
                .totalRounds(totalRounds)
                .build();
    }


} 