package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.entity.TmiVote;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TmiVotes {
    private final List<TmiVote> votes;

    private TmiVotes(List<TmiVote> votes) {
        this.votes = votes != null ? votes : Collections.emptyList();
    }

    public static TmiVotes from(List<TmiVote> votes) {
        return new TmiVotes(votes);
    }

    public TmiVote findVoteByName(String memberName) {
        return votes.stream()
                .filter(vote -> vote.getVoterName().equals(memberName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("투표자 이름으로 투표를 찾을 수 없습니다: " + memberName));
    }

    public Map<String, Long> getVotingResults() {
        return votes.stream()
                .collect(Collectors.groupingBy(
                        TmiVote::getVotedMemberName,
                        HashMap::new,
                        Collectors.counting()
                ));
    }
}
