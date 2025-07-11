package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceChoice;
import lombok.Builder;

import java.util.List;

@Builder
public record BalanceRoundResultResponse(
        BalanceChoice myChoice,
        int choiceACount,
        int choiceBCount,
        double choiceAPercentage,
        double choiceBPercentage,
        BalanceChoice majorityChoice,
        boolean isTie,
        int scoreChange,
        int currentScore,
        int currentRank,
        int currentRound,
        List<BalanceMemberScoreInfo> allMemberScores
) {
} 