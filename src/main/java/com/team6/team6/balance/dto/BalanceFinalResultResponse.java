package com.team6.team6.balance.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BalanceFinalResultResponse(
        String memberName,
        int finalScore,
        int finalRank,
        List<String> winnerNicknames,                    // 우승자 닉네임들 (공동 우승 가능)
        List<BalanceQuestionSummary> mostBalancedQuestions,     // 가장 비율이 비슷했던 질문들
        List<BalanceQuestionSummary> mostUnanimousQuestions     // 가장 만장일치에 가까웠던 질문들
) {
    
    public static BalanceFinalResultResponse of(String memberName, int finalScore, int finalRank,
                                              List<String> winnerNicknames,
                                              List<BalanceQuestionSummary> mostBalancedQuestions,
                                              List<BalanceQuestionSummary> mostUnanimousQuestions) {
        return BalanceFinalResultResponse.builder()
                .memberName(memberName)
                .finalScore(finalScore)
                .finalRank(finalRank)
                .winnerNicknames(winnerNicknames)
                .mostBalancedQuestions(mostBalancedQuestions)
                .mostUnanimousQuestions(mostUnanimousQuestions)
                .build();
    }
} 