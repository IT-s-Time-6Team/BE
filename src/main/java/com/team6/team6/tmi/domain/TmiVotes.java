package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.dto.TopVoter;
import com.team6.team6.tmi.dto.VoteResult;
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

    public int countCorrectVotesForVoter(String voterName) {
        return (int) votes.stream()
                .filter(vote -> vote.getVoterName().equals(voterName))
                .filter(vote -> vote.getIsCorrect() != null && vote.getIsCorrect())
                .count();
    }

    public int countIncorrectVotesForMember(String votedMemberName) {
        return (int) votes.stream()
                .filter(vote -> vote.getVotedMemberName().equals(votedMemberName))
                .filter(vote -> Boolean.FALSE.equals(vote.getIsCorrect()))
                .count();
    }

    public VoteResult calculateMemberVoteResult(String memberName) {
        int correctCount = countCorrectVotesForVoter(memberName);
        long totalVotes = votes.stream()
                .filter(vote -> vote.getVoterName().equals(memberName))
                .count();
        int incorrectCount = (int) (totalVotes - correctCount);
        return new VoteResult(correctCount, incorrectCount);
    }

    public List<TopVoter> getTopVoters() {
        Map<String, Integer> correctCountByMember = new HashMap<>();

        for (TmiVote vote : votes) {
            if (vote.getIsCorrect() != null && vote.getIsCorrect()) {
                String voterName = vote.getVoterName();
                correctCountByMember.put(voterName,
                        correctCountByMember.getOrDefault(voterName, 0) + 1);
            }
        }

        if (correctCountByMember.isEmpty()) {
            return Collections.emptyList();
        }

        int maxCorrectCount = Collections.max(correctCountByMember.values());

        return correctCountByMember.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCorrectCount)
                .map(entry -> new TopVoter(
                        entry.getKey(), entry.getValue()
                ))
                .collect(Collectors.toList());
    }
}
