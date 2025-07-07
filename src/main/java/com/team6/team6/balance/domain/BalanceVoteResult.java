package com.team6.team6.balance.domain;

import com.team6.team6.balance.entity.BalanceChoice;

public record BalanceVoteResult(
        long choiceACount,
        long choiceBCount,
        BalanceChoice majorityChoice,
        boolean isTie
) {
    
    public static BalanceVoteResult of(long choiceACount, long choiceBCount, 
                                     BalanceChoice majorityChoice, boolean isTie) {
        return new BalanceVoteResult(choiceACount, choiceBCount, majorityChoice, isTie);
    }
    
    public double getChoiceAPercentage() {
        long total = choiceACount + choiceBCount;
        if (total == 0) return 0.0;
        return (double) choiceACount / total * 100;
    }
    
    public double getChoiceBPercentage() {
        long total = choiceACount + choiceBCount;
        if (total == 0) return 0.0;
        return (double) choiceBCount / total * 100;
    }
} 