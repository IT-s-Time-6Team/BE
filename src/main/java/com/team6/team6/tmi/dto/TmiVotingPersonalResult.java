package com.team6.team6.tmi.dto;

import com.team6.team6.member.entity.CharacterType;

import java.util.Map;

public record TmiVotingPersonalResult(
        String tmiContent,
        String correctAnswer,
        CharacterType answerMemberCharacterType,
        String myVote,
        CharacterType myCharacterType,
        boolean isCorrect,
        Map<String, Long> votingResults,
        int round
) {
    public static TmiVotingPersonalResult of(
            String tmiContent,
            String correctAnswer,
            CharacterType answerMemberCharacterType,
            String myVote,
            CharacterType myCharacterType,
            boolean isCorrect,
            Map<String, Long> votingResults,
            int round
    ) {
        return new TmiVotingPersonalResult(tmiContent, correctAnswer, answerMemberCharacterType,
                myVote, myCharacterType, isCorrect, votingResults, round);
    }
}
