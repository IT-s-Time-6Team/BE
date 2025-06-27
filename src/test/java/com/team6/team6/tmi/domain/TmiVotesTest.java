package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class TmiVotesTest {

    @Test
    @DisplayName("빈 투표 목록으로 TmiVotes를 생성할 수 있다")
    void createFromEmptyList() {
        // when
        TmiVotes tmiVotes = TmiVotes.from(null);

        // then
        assertThat(tmiVotes).isNotNull();
    }

    @Test
    @DisplayName("투표자의 투표를 조회할 수 있다")
    void getMyVote() {
        // given
        List<TmiVote> votes = List.of(
                createTmiVote("voter1", "member1"),
                createTmiVote("voter2", "member2"),
                createTmiVote("voter3", "member1")
        );
        TmiVotes tmiVotes = TmiVotes.from(votes);

        // when
        TmiVote myVote = tmiVotes.getMyVote("voter2");

        // then
        assertThat(myVote.getVoterName()).isEqualTo("voter2");
        assertThat(myVote.getVotedMemberName()).isEqualTo("member2");
    }

    @Test
    @DisplayName("투표 결과를 집계할 수 있다")
    void getVotingResults() {
        // given
        List<TmiVote> votes = List.of(
                createTmiVote("voter1", "member1"),
                createTmiVote("voter2", "member1"),
                createTmiVote("voter3", "member2"),
                createTmiVote("voter4", "member1")
        );
        TmiVotes tmiVotes = TmiVotes.from(votes);

        // when
        Map<String, Long> results = tmiVotes.getVotingResults();

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get("member1")).isEqualTo(3L);
        assertThat(results.get("member2")).isEqualTo(1L);
    }

    private TmiVote createTmiVote(String voterName, String votedMemberName) {
        return TmiVote.create(1L, voterName, votedMemberName, 1L, 0);
    }
}
