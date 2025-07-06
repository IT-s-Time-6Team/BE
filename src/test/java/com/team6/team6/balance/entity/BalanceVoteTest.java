package com.team6.team6.balance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("BalanceVote 엔티티 테스트")
class BalanceVoteTest {

    @Test
    @DisplayName("BalanceVote 생성 테스트")
    void createBalanceVoteTest() {
        // given
        Long roomId = 1L;
        String voterName = "testUser";
        Long memberId = 123L;
        BalanceChoice selectedChoice = BalanceChoice.A;
        Integer votingRound = 1;
        Long balanceSessionQuestionId = 456L;

        // when
        BalanceVote vote = BalanceVote.create(roomId, voterName, memberId, selectedChoice, votingRound, balanceSessionQuestionId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(vote.getRoomId()).isEqualTo(roomId);
            softly.assertThat(vote.getVoterName()).isEqualTo(voterName);
            softly.assertThat(vote.getMemberId()).isEqualTo(memberId);
            softly.assertThat(vote.getSelectedChoice()).isEqualTo(selectedChoice);
            softly.assertThat(vote.getVotingRound()).isEqualTo(votingRound);
            softly.assertThat(vote.getBalanceSessionQuestionId()).isEqualTo(balanceSessionQuestionId);
        });
    }

    @Test
    @DisplayName("BalanceChoice A 선택 테스트")
    void selectChoiceATest() {
        // given & when
        BalanceVote vote = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);

        // then
        assertThat(vote.getSelectedChoice()).isEqualTo(BalanceChoice.A);
    }

    @Test
    @DisplayName("BalanceChoice B 선택 테스트")
    void selectChoiceBTest() {
        // given & when
        BalanceVote vote = BalanceVote.create(1L, "user1", 1L, BalanceChoice.B, 1, 1L);

        // then
        assertThat(vote.getSelectedChoice()).isEqualTo(BalanceChoice.B);
    }

    @Test
    @DisplayName("다른 라운드 투표 테스트")
    void differentRoundVoteTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.B, 2, 2L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(vote1.getVotingRound()).isEqualTo(1);
            softly.assertThat(vote2.getVotingRound()).isEqualTo(2);
            softly.assertThat(vote1.getSelectedChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(vote2.getSelectedChoice()).isEqualTo(BalanceChoice.B);
        });
    }

    @Test
    @DisplayName("같은 방 다른 사용자 투표 테스트")
    void sameRoomDifferentUserTest() {
        // given
        Long roomId = 1L;
        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.B, 1, 1L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(vote1.getRoomId()).isEqualTo(vote2.getRoomId());
            softly.assertThat(vote1.getVoterName()).isNotEqualTo(vote2.getVoterName());
            softly.assertThat(vote1.getMemberId()).isNotEqualTo(vote2.getMemberId());
            softly.assertThat(vote1.getVotingRound()).isEqualTo(vote2.getVotingRound());
        });
    }
} 