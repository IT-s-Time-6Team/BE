package com.team6.team6.balance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("BalanceMemberScore 엔티티 테스트")
class BalanceMemberScoreTest {

    @Test
    @DisplayName("초기 BalanceMemberScore 생성 테스트")
    void createInitialTest() {
        // given
        Long roomId = 1L;
        Long memberId = 123L;
        String memberName = "testUser";

        // when
        BalanceMemberScore score = BalanceMemberScore.createInitial(roomId, memberId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getRoomId()).isEqualTo(roomId);
            softly.assertThat(score.getMemberId()).isEqualTo(memberId);
            softly.assertThat(score.getMemberName()).isEqualTo(memberName);
            softly.assertThat(score.getCurrentScore()).isZero();
            softly.assertThat(score.getTotalCorrect()).isZero();
            softly.assertThat(score.getTotalWrong()).isZero();
        });
    }

    @Test
    @DisplayName("점수 추가 테스트 - 양수")
    void addPositiveScoreTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.addScore(5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(5);
            softly.assertThat(score.getTotalCorrect()).isEqualTo(1);
            softly.assertThat(score.getTotalWrong()).isZero();
        });
    }

    @Test
    @DisplayName("점수 추가 테스트 - 음수")
    void addNegativeScoreTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.addScore(-3);

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(-3);
            softly.assertThat(score.getTotalCorrect()).isZero();
            softly.assertThat(score.getTotalWrong()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("점수 추가 테스트 - 0점")
    void addZeroScoreTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.addScore(0);

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isZero();
            softly.assertThat(score.getTotalCorrect()).isZero();
            softly.assertThat(score.getTotalWrong()).isZero();
        });
    }

    @Test
    @DisplayName("다수파 선택 시 점수 업데이트")
    void updateScoreMajorityTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.updateScore(true); // 다수파

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(1);
            softly.assertThat(score.getTotalCorrect()).isEqualTo(1);
            softly.assertThat(score.getTotalWrong()).isZero();
        });
    }

    @Test
    @DisplayName("소수파 선택 시 점수 업데이트")
    void updateScoreMinorityTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.updateScore(false); // 소수파

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(-1);
            softly.assertThat(score.getTotalCorrect()).isZero();
            softly.assertThat(score.getTotalWrong()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("여러 라운드 점수 누적 테스트")
    void multipleRoundsScoreTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.updateScore(true);   // +1 (다수파)
        score.updateScore(false);  // -1 (소수파)
        score.updateScore(true);   // +1 (다수파)
        score.updateScore(true);   // +1 (다수파)

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(2); // 1-1+1+1 = 2
            softly.assertThat(score.getTotalCorrect()).isEqualTo(3); // 다수파 3번
            softly.assertThat(score.getTotalWrong()).isEqualTo(1);   // 소수파 1번
        });
    }

    @Test
    @DisplayName("점수가 음수가 되는 경우 테스트")
    void negativeScoreTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.updateScore(false); // -1
        score.updateScore(false); // -1
        score.updateScore(false); // -1

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(-3);
            softly.assertThat(score.getTotalCorrect()).isZero();
            softly.assertThat(score.getTotalWrong()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("점수가 0에서 시작해서 변동하는 테스트")
    void scoreFluctuationTest() {
        // given
        BalanceMemberScore score = BalanceMemberScore.createInitial(1L, 1L, "user1");

        // when
        score.addScore(5);   // +5
        score.addScore(-3);  // -3
        score.addScore(2);   // +2

        // then
        assertSoftly(softly -> {
            softly.assertThat(score.getCurrentScore()).isEqualTo(4); // 0+5-3+2 = 4
            softly.assertThat(score.getTotalCorrect()).isEqualTo(2); // 양수 점수 2번
            softly.assertThat(score.getTotalWrong()).isEqualTo(1);   // 음수 점수 1번
        });
    }
} 