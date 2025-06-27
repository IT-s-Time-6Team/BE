package com.team6.team6.tmi.dto;

import java.util.List;

public record TmiVotingStartResponse(
        String tmiContent,
        int round,
        List<String> members
) {
    public static TmiVotingStartResponse of(String tmiContent, int round, List<String> members) {
        return new TmiVotingStartResponse(tmiContent, round, members);
    }
}
