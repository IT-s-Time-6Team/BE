package com.team6.team6.balance.domain;

import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceVote;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class BalanceVotes {
    private final List<BalanceVote> votes;

    private BalanceVotes(List<BalanceVote> votes) {
        this.votes = votes != null ? votes : Collections.emptyList();
    }

    public static BalanceVotes from(List<BalanceVote> votes) {
        return new BalanceVotes(votes);
    }

    public BalanceVote findVoteByName(String memberName) {
        return votes.stream()
                .filter(vote -> vote.getVoterName().equals(memberName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("투표자 이름으로 투표를 찾을 수 없습니다: " + memberName));
    }

    public BalanceVoteResult calculateVoteResult() {
        Map<BalanceChoice, Long> choiceCounts = votes.stream()
                .collect(Collectors.groupingBy(
                        BalanceVote::getSelectedChoice,
                        HashMap::new,
                        Collectors.counting()
                ));

        long choiceACount = choiceCounts.getOrDefault(BalanceChoice.A, 0L);
        long choiceBCount = choiceCounts.getOrDefault(BalanceChoice.B, 0L);
        
        BalanceChoice majorityChoice = null;
        boolean isTie = choiceACount == choiceBCount;
        
        if (!isTie) {
            majorityChoice = choiceACount > choiceBCount ? BalanceChoice.A : BalanceChoice.B;
        }

        return BalanceVoteResult.of(
                choiceACount,
                choiceBCount,
                majorityChoice,
                isTie
        );
    }

    public BalanceChoice getMajorityChoice() {
        BalanceVoteResult result = calculateVoteResult();
        return result.majorityChoice();
    }

    public boolean isTie() {
        BalanceVoteResult result = calculateVoteResult();
        return result.isTie();
    }

    public int getTotalVoteCount() {
        return votes.size();
    }

    public boolean isEmpty() {
        return votes.isEmpty();
    }
} 