package com.team6.team6.balance.dto;

import com.team6.team6.balance.entity.BalanceMemberScore;
import lombok.Builder;

@Builder
public record BalanceMemberScoreInfo(
        String memberName,
        int currentScore,
        int scoreChange,
        int rank
) {
    
    public static BalanceMemberScoreInfo of(String memberName, int currentScore, 
                                          int scoreChange, int rank) {
        return BalanceMemberScoreInfo.builder()
                .memberName(memberName)
                .currentScore(currentScore)
                .scoreChange(scoreChange)
                .rank(rank)
                .build();
    }
    
    public static BalanceMemberScoreInfo from(BalanceMemberScore score, int rank) {
        return BalanceMemberScoreInfo.builder()
                .memberName(score.getMemberName())
                .currentScore(score.getCurrentScore())
                .scoreChange(0) // 변화량은 별도 계산 필요
                .rank(rank)
                .build();
    }
} 