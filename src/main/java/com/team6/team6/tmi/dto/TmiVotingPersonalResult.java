package com.team6.team6.tmi.dto;

import java.util.Map;

public record TmiVotingPersonalResult(
        String tmiContent,
        String correctAnswer,
        String myVote,
        boolean isCorrect,
        Map<String, Long> votingResults,
        int round
) {
    public static TmiVotingPersonalResult of(
            String tmiContent,
            String correctAnswer,
            String myVote,
            boolean isCorrect,
            Map<String, Long> votingResults,
            int round
    ) {
        return new TmiVotingPersonalResult(tmiContent, correctAnswer, myVote, isCorrect, votingResults, round);
    }
}
