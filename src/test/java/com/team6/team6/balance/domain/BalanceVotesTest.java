package com.team6.team6.balance.domain;

import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("BalanceVotes 도메인 테스트")
class BalanceVotesTest {

    @Test
    @DisplayName("빈 투표 목록으로 BalanceVotes 생성")
    void createEmptyBalanceVotesTest() {
        // given & when
        BalanceVotes votes = BalanceVotes.from(List.of());

        // then
        assertSoftly(softly -> {
            softly.assertThat(votes.isEmpty()).isTrue();
            softly.assertThat(votes.getTotalVoteCount()).isZero();
        });
    }

    @Test
    @DisplayName("null 투표 목록으로 BalanceVotes 생성")
    void createNullBalanceVotesTest() {
        // given & when
        BalanceVotes votes = BalanceVotes.from(null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(votes.isEmpty()).isTrue();
            softly.assertThat(votes.getTotalVoteCount()).isZero();
        });
    }

    @Test
    @DisplayName("투표자 이름으로 투표 찾기")
    void findVoteByNameTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2));

        // when
        BalanceVote foundVote = votes.findVoteByName("user1");

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundVote.getVoterName()).isEqualTo("user1");
            softly.assertThat(foundVote.getSelectedChoice()).isEqualTo(BalanceChoice.A);
        });
    }

    @Test
    @DisplayName("존재하지 않는 투표자 이름으로 찾기 시 예외")
    void findVoteByNameNotFoundTest() {
        // given
        BalanceVote vote = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote));

        // when & then
        assertThatThrownBy(() -> votes.findVoteByName("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("투표자 이름으로 투표를 찾을 수 없습니다: nonexistent");
    }

    @Test
    @DisplayName("A가 다수파인 경우 투표 결과 계산")
    void calculateVoteResultChoiceAMajorityTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.A, 1, 1L);
        BalanceVote vote3 = BalanceVote.create(1L, "user3", 3L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2, vote3));

        // when
        BalanceVoteResult result = votes.calculateVoteResult();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(2);
            softly.assertThat(result.choiceBCount()).isEqualTo(1);
            softly.assertThat(result.majorityChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.isTie()).isFalse();
        });
    }

    @Test
    @DisplayName("B가 다수파인 경우 투표 결과 계산")
    void calculateVoteResultChoiceBMajorityTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.B, 1, 1L);
        BalanceVote vote3 = BalanceVote.create(1L, "user3", 3L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2, vote3));

        // when
        BalanceVoteResult result = votes.calculateVoteResult();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(1);
            softly.assertThat(result.choiceBCount()).isEqualTo(2);
            softly.assertThat(result.majorityChoice()).isEqualTo(BalanceChoice.B);
            softly.assertThat(result.isTie()).isFalse();
        });
    }

    @Test
    @DisplayName("동점인 경우 투표 결과 계산")
    void calculateVoteResultTieTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.A, 1, 1L);
        BalanceVote vote3 = BalanceVote.create(1L, "user3", 3L, BalanceChoice.B, 1, 1L);
        BalanceVote vote4 = BalanceVote.create(1L, "user4", 4L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2, vote3, vote4));

        // when
        BalanceVoteResult result = votes.calculateVoteResult();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(2);
            softly.assertThat(result.choiceBCount()).isEqualTo(2);
            softly.assertThat(result.majorityChoice()).isNull();
            softly.assertThat(result.isTie()).isTrue();
        });
    }

    @Test
    @DisplayName("모든 투표가 A인 경우")
    void allVotesChoiceATest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.A, 1, 1L);
        BalanceVote vote3 = BalanceVote.create(1L, "user3", 3L, BalanceChoice.A, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2, vote3));

        // when & then
        assertSoftly(softly -> {
            softly.assertThat(votes.getMajorityChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(votes.isTie()).isFalse();
            softly.assertThat(votes.getTotalVoteCount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("모든 투표가 B인 경우")
    void allVotesChoiceBTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.B, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2));

        // when & then
        assertSoftly(softly -> {
            softly.assertThat(votes.getMajorityChoice()).isEqualTo(BalanceChoice.B);
            softly.assertThat(votes.isTie()).isFalse();
            softly.assertThat(votes.getTotalVoteCount()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("단일 투표인 경우")
    void singleVoteTest() {
        // given
        BalanceVote vote = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote));

        // when
        BalanceVoteResult result = votes.calculateVoteResult();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(1);
            softly.assertThat(result.choiceBCount()).isZero();
            softly.assertThat(result.majorityChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.isTie()).isFalse();
            softly.assertThat(votes.getTotalVoteCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("동점인 경우 다수파 선택은 null")
    void tieVoteMajorityChoiceTest() {
        // given
        BalanceVote vote1 = BalanceVote.create(1L, "user1", 1L, BalanceChoice.A, 1, 1L);
        BalanceVote vote2 = BalanceVote.create(1L, "user2", 2L, BalanceChoice.B, 1, 1L);
        BalanceVotes votes = BalanceVotes.from(List.of(vote1, vote2));

        // when
        BalanceChoice majorityChoice = votes.getMajorityChoice();

        // then
        assertSoftly(softly -> {
            softly.assertThat(majorityChoice).isNull();
            softly.assertThat(votes.isTie()).isTrue();
        });
    }
} 