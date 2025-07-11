package com.team6.team6.balance.dto;

import lombok.Builder;

@Builder
public record BalanceQuestionSummary(
        int round,
        String questionA,
        String questionB
) {
    
    public static BalanceQuestionSummary of(int round, String questionA, String questionB) {
        return BalanceQuestionSummary.builder()
                .round(round)
                .questionA(questionA)
                .questionB(questionB)
                .build();
    }
} 