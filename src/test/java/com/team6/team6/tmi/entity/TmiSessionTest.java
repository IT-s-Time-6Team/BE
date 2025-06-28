package com.team6.team6.tmi.entity;

import com.team6.team6.tmi.domain.VoteResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TmiSessionTest {

    @Test
    void TMI_개수_증가와_수집_진행률_계산_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 4);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount(); // 2개 제출

        // when
        int progress = session.calculateCollectionProgress();

        // then
        assertSoftly(softly -> {
            softly.assertThat(progress).isEqualTo(50); // 2/4 * 100 = 50%
            softly.assertThat(session.getSubmittedTmiCount()).isEqualTo(2);
        });
    }

    @Test
    void 모든_TMI_수집_완료_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();

        // when
        boolean isCompleted = session.isAllTmiCollected();

        // then
        assertSoftly(softly -> {
            softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.HINT);
            softly.assertThat(isCompleted).isTrue();
        });
    }

    @Test
    void TMI_수집_미완료_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 4);
        session.incrementSubmittedTmiCount(); // 1개만 제출

        // when
        boolean isCompleted = session.isAllTmiCollected();

        // then
        assertSoftly(softly -> {
            softly.assertThat(isCompleted).isFalse();
            softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.COLLECTING_TMI);
        });
    }

    @Nested
    @DisplayName("상태 검증 테스트")
    class StateValidationTest {

        @Test
        @DisplayName("TMI 수집 단계가 아닐 때 예외 발생")
        void requireCollectingTmiPhaseTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount();
            session.startHintTime();

            // when, then
            assertThatThrownBy(session::requireCollectingTmiPhase)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("TMI 수집 단계가 아닙니다");
        }

        @Test
        @DisplayName("힌트 단계가 아닐 때 예외 발생")
        void requireHintPhaseTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);

            // when, then
            assertThatThrownBy(session::requireHintPhase)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("힌트 단계가 아닙니다");
        }

        @Test
        @DisplayName("모든 TMI가 수집되지 않았을 때 힌트 시작 불가")
        void validateCanStartHintFailTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);
            session.incrementSubmittedTmiCount(); // 1/3만 제출

            // when, then
            assertThatThrownBy(session::validateCanStartHint)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("모든 TMI가 수집되지 않았습니다");
        }

        @Test
        @DisplayName("힌트 단계가 아닐 때 투표 시작 불가")
        void validateCanStartVotingFailTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);

            // when, then
            assertThatThrownBy(session::validateCanStartVoting)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("투표를 시작할 수 없는 상태입니다");
        }
    }

    @Nested
    @DisplayName("상태 전환 테스트")
    class StateTransitionTest {

        @Test
        @DisplayName("힌트 단계 시작")
        void startHintTimeTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 2);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount();
            session.startHintTime();

            // then
            assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.HINT);
        }

        @Test
        @DisplayName("투표 단계 시작")
        void startVotingPhaseTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 2);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount(); // 모든 TMI 제출
            session.startHintTime(); // HINT 단계로 전환

            // when
            session.startVotingPhase();

            // then
            assertSoftly(softly -> {
                softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.VOTING);
                softly.assertThat(session.getCurrentVotingTmiIndex()).isZero();
                softly.assertThat(session.getCurrentVotedMemberCount()).isZero();
            });
        }
    }

    @Nested
    @DisplayName("투표 처리 테스트")
    class ProcessVoteTest {

        @Test
        @DisplayName("투표 진행 중 상태 반환")
        void processVoteInProgressTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount(); // 모든 TMI 제출
            session.startHintTime();
            session.startVotingPhase(); // 투표 단계로 전환

            // when
            VoteResult result = session.processVote(); // 1/3만 투표

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteResult.IN_PROGRESS);
                softly.assertThat(session.getCurrentVotedMemberCount()).isEqualTo(1);
                softly.assertThat(session.getCurrentVotingTmiIndex()).isZero();
            });
        }

        @Test
        @DisplayName("라운드 완료 후 다음 TMI로 이동")
        void processVoteRoundCompletedTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 3);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount(); // 모든 TMI 제출
            session.startHintTime();
            session.startVotingPhase(); // 투표 단계로 전환

            // when
            session.processVote(); // 1/3 투표
            session.processVote(); // 2/3 투표
            VoteResult result = session.processVote(); // 3/3 투표 (라운드 완료)

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteResult.ROUND_COMPLETED);
                softly.assertThat(session.getCurrentVotedMemberCount()).isZero(); // 초기화됨
                softly.assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(1); // 다음 인덱스로 이동
                softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.VOTING); // 여전히 투표 단계
            });
        }

        @Test
        @DisplayName("모든 TMI 투표 완료 후 게임 완료")
        void processVoteAllCompletedTest() {
            // given
            TmiSession session = TmiSession.createInitialSession(1L, 2);
            session.incrementSubmittedTmiCount();
            session.incrementSubmittedTmiCount(); // 모든 TMI 제출
            session.startHintTime();
            session.startVotingPhase(); // 투표 단계로 전환

            // 첫 번째 TMI 투표 완료
            session.processVote();
            session.processVote();

            // 두 번째(마지막) TMI 투표 시작
            assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(1);

            // when
            session.processVote();
            VoteResult result = session.processVote(); // 마지막 투표 완료

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteResult.ALL_COMPLETED);
                softly.assertThat(session.getCurrentStep()).isEqualTo(TmiGameStep.COMPLETED);
                softly.assertThat(session.getClosedAt()).isNotNull();
            });
        }
    }

    @Test
    @DisplayName("라운드 완료 여부 확인")
    void isCurrentRoundVotingCompletedTest() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount(); // 모든 TMI 제출
        session.startHintTime();
        session.startVotingPhase(); // 투표 단계로 전환

        // when - then
        assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(0);

        session.processVote(); // 1명 투표
        assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(0);

        session.processVote(); // 2명 투표 (모두 완료)
        assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("마지막 TMI 인덱스 확인")
    void isLastTmiIndexTest() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount(); // 모든 TMI 제출
        session.startHintTime();
        session.startVotingPhase(); // 투표 단계로 전환

        // when - then
        assertThat(session.isLastTmiIndex()).isFalse(); // 첫 번째 인덱스 (0)

        // 첫 번째 라운드 투표 완료
        session.processVote();
        session.processVote();
        session.processVote();

        // 두 번째 라운드로 이동
        assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(1);
        assertThat(session.isLastTmiIndex()).isFalse(); // 두 번째 인덱스 (1)

        // 두 번째 라운드 투표 완료
        session.processVote();
        session.processVote();
        session.processVote();

        // 세 번째(마지막) 라운드로 이동
        assertThat(session.getCurrentVotingTmiIndex()).isEqualTo(2);
        assertThat(session.isLastTmiIndex()).isTrue(); // 마지막 인덱스 (2)
    }
}
