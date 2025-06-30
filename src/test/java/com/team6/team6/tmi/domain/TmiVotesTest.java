package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TmiVotesTest {

    @Test
    void 투표자의_투표_조회_테스트() {
        // given
        List<TmiVote> votes = List.of(
                createTmiVote("voter1", "member1"),
                createTmiVote("voter2", "member2"),
                createTmiVote("voter3", "member1")
        );
        TmiVotes tmiVotes = TmiVotes.from(votes);

        // when
        TmiVote myVote = tmiVotes.findVoteByName("voter2");

        // then
        assertSoftly(softly->{
            softly.assertThat(myVote.getVoterName()).isEqualTo("voter2");
            softly.assertThat(myVote.getVotedMemberName()).isEqualTo("member2");
        });
    }

    @Test
    void 투표자의_투표_조회_예외_테스트() {
        // given
        List<TmiVote> votes = List.of(
                createTmiVote("voter1", "member1"),
                createTmiVote("voter2", "member2")
        );
        TmiVotes tmiVotes = TmiVotes.from(votes);

        // when & then
        assertThatThrownBy(() -> tmiVotes.findVoteByName("nonExistentVoter"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("투표자 이름으로 투표를 찾을 수 없습니다: nonExistentVoter");
    }

    @Test
    void 투표_결과_집계_테스트() {
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
        assertSoftly(softly->{
            softly.assertThat(results).hasSize(2);
            softly.assertThat(results.get("member1")).isEqualTo(3L);
            softly.assertThat(results.get("member2")).isEqualTo(1L);
        });
    }

    private TmiVote createTmiVote(String voterName, String votedMemberName) {
        return TmiVote.create(1L, voterName, votedMemberName, 1L, 0);
    }
}
