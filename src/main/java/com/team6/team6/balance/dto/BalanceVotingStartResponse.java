package com.team6.team6.balance.dto;

public record BalanceVotingStartResponse(
        String questionA,
        String questionB
) {
    public static BalanceVotingStartResponse of(String questionA, String questionB) {
        return new BalanceVotingStartResponse(questionA, questionB);
    }
} 