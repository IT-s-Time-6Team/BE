package com.team6.team6.balance.entity;

import com.team6.team6.balance.domain.VoteStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("BalanceSession 엔티티 테스트")
class BalanceSessionTest {

    @Test
    @DisplayName("초기 BalanceSession 생성 테스트")
    void createInitialSessionTest() {
        // given
        Long roomId = 1L;
        int totalMembers = 4;
        int totalQuestions = 3;

        // when
        BalanceSession session = BalanceSession.createInitialSession(roomId, totalMembers, totalQuestions);

        // then
        assertSoftly(softly -> {
            softly.assertThat(session.getRoomId()).isEqualTo(roomId);
            softly.assertThat(session.getTotalMembers()).isEqualTo(totalMembers);
            softly.assertThat(session.getTotalQuestions()).isEqualTo(totalQuestions);
            softly.assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.WAITING_FOR_MEMBERS);
            softly.assertThat(session.getCurrentQuestionIndex()).isZero();
            softly.assertThat(session.getCurrentVotedMemberCount()).isZero();
            softly.assertThat(session.getCurrentResultViewedCount()).isZero();
        });
    }

    @Nested
    @DisplayName("멤버 관리 테스트")
    class MemberManagementTest {

        @Test
        @DisplayName("모든 멤버 입장 완료 확인")
        void isAllMembersJoinedTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 2);
            int currentMemberCount = 3;

            // when
            boolean result = session.isAllMembersJoined(currentMemberCount);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("멤버 입장 미완료 확인")
        void isAllMembersJoinedFalseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 2);
            int currentMemberCount = 2;

            // when
            boolean result = session.isAllMembersJoined(currentMemberCount);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("멤버 입장 진행률 계산")
        void getMemberJoinProgressTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
            int currentMemberCount = 2;

            // when
            int progress = session.getMemberJoinProgress(currentMemberCount);

            // then
            assertThat(progress).isEqualTo(50); // 2/4 * 100 = 50%
        }
    }

    @Nested
    @DisplayName("게임 상태 전환 테스트")
    class GameStateTransitionTest {

        @Test
        @DisplayName("문제 공개 시작")
        void startQuestionRevealPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when
            session.startQuestionRevealPhase();

            // then
            assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.QUESTION_REVEAL);
        }

        @Test
        @DisplayName("토론 시작")
        void startDiscussionPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.startQuestionRevealPhase();

            // when
            session.startDiscussionPhase();

            // then
            assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.DISCUSSION);
        }

        @Test
        @DisplayName("투표 시작")
        void startVotingPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();

            // when
            session.startVotingPhase();

            // then
            assertSoftly(softly -> {
                softly.assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.VOTING);
                softly.assertThat(session.getCurrentVotedMemberCount()).isZero();
            });
        }

        @Test
        @DisplayName("결과 확인 시작")
        void startResultViewPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 2, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();
            // 모든 투표 완료
            session.processVote();
            session.processVote();

            // when
            session.startResultViewPhase();

            // then
            assertSoftly(softly -> {
                softly.assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.RESULT_VIEW);
                softly.assertThat(session.getCurrentResultViewedCount()).isZero();
            });
        }
    }

    @Nested
    @DisplayName("라운드 진행 테스트")
    class RoundProgressTest {

        @Test
        @DisplayName("다음 문제로 이동")
        void moveToNextQuestionTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when
            session.moveToNextQuestion();

            // then
            assertSoftly(softly -> {
                softly.assertThat(session.getCurrentQuestionIndex()).isEqualTo(1);
                softly.assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.WAITING_FOR_MEMBERS);
                softly.assertThat(session.getCurrentVotedMemberCount()).isZero();
                softly.assertThat(session.getCurrentResultViewedCount()).isZero();
            });
        }

        @Test
        @DisplayName("마지막 문제 인덱스 확인")
        void isLastQuestionIndexTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            // 마지막 문제로 이동 (0, 1, 2 중 2번째)
            session.moveToNextQuestion();
            session.moveToNextQuestion();

            // then
            assertThat(session.isLastQuestionIndex()).isTrue();
        }

        @Test
        @DisplayName("마지막 문제가 아닌 경우")
        void isNotLastQuestionIndexTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.moveToNextQuestion(); // 1번째 문제

            // then
            assertThat(session.isLastQuestionIndex()).isFalse();
        }

        @Test
        @DisplayName("더 많은 문제가 있는지 확인")
        void hasMoreQuestionsTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // then
            assertThat(session.hasMoreQuestions()).isTrue();
        }

        @Test
        @DisplayName("더 이상 문제가 없는 경우")
        void hasNoMoreQuestionsTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 2);
            session.moveToNextQuestion();
            session.moveToNextQuestion(); // 2번째 문제 (인덱스 2)

            // then
            assertThat(session.hasMoreQuestions()).isFalse();
        }

        @Test
        @DisplayName("게임 완료 처리")
        void completeGameTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when
            session.completeGame();

            // then
            assertSoftly(softly -> {
                softly.assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.COMPLETED);
                softly.assertThat(session.getClosedAt()).isNotNull();
            });
        }
    }

    @Nested
    @DisplayName("투표 처리 테스트")
    class VoteHandlingTest {

        @Test
        @DisplayName("투표 진행 중 상태")
        void processVoteInProgressTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();

            // when
            VoteStatus result = session.processVote();

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteStatus.IN_PROGRESS);
                softly.assertThat(session.getCurrentVotedMemberCount()).isEqualTo(1);
            });
        }

        @Test
        @DisplayName("라운드 완료")
        void processVoteRoundCompletedTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 2, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();

            // when
            session.processVote();
            VoteStatus result = session.processVote(); // 모든 투표 완료

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteStatus.ROUND_COMPLETED);
                softly.assertThat(session.getCurrentVotedMemberCount()).isEqualTo(2);
                softly.assertThat(session.isCurrentRoundVotingCompleted()).isTrue();
            });
        }

        @Test
        @DisplayName("모든 게임 완료")
        void processVoteAllCompletedTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 2, 1);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();

            // when
            session.processVote();
            VoteStatus result = session.processVote(); // 마지막 문제 투표 완료

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isEqualTo(VoteStatus.ALL_COMPLETED);
                softly.assertThat(session.isLastQuestionIndex()).isTrue();
            });
        }

        @Test
        @DisplayName("투표 진행률 계산")
        void getCurrentRoundVotingProgressTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();
            session.processVote();
            session.processVote(); // 2/4 투표 완료

            // when
            int progress = session.getCurrentRoundVotingProgress();

            // then
            assertThat(progress).isEqualTo(50); // 2/4 * 100 = 50%
        }
    }

    @Nested
    @DisplayName("결과 확인 처리 테스트")
    class ResultViewHandlingTest {

        @Test
        @DisplayName("결과 확인 처리")
        void processResultViewReadyTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();
            session.processVote();
            session.processVote();
            session.processVote();
            session.startResultViewPhase();

            // when
            boolean result1 = session.processResultViewReady();
            boolean result2 = session.processResultViewReady();

            // then
            assertSoftly(softly -> {
                softly.assertThat(result1).isFalse(); // 아직 모든 사람이 완료하지 않음
                softly.assertThat(result2).isFalse(); // 아직 한 명 더 남음
                softly.assertThat(session.getCurrentResultViewedCount()).isEqualTo(2);
            });
        }

        @Test
        @DisplayName("모든 결과 확인 완료")
        void processResultViewReadyAllCompletedTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 2, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();
            session.processVote();
            session.processVote();
            session.startResultViewPhase();

            // when
            session.processResultViewReady();
            boolean result = session.processResultViewReady(); // 모든 사람 완료

            // then
            assertSoftly(softly -> {
                softly.assertThat(result).isTrue(); // 모든 사람 완료
                softly.assertThat(session.isAllResultViewCompleted()).isTrue();
            });
        }

        @Test
        @DisplayName("결과 확인 진행률 계산")
        void getResultViewProgressTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
            session.startQuestionRevealPhase();
            session.startDiscussionPhase();
            session.startVotingPhase();
            session.processVote();
            session.processVote();
            session.processVote();
            session.processVote();
            session.startResultViewPhase();
            session.processResultViewReady();
            session.processResultViewReady(); // 2/4 완료

            // when
            int progress = session.getResultViewProgress();

            // then
            assertThat(progress).isEqualTo(50); // 2/4 * 100 = 50%
        }
    }

    @Nested
    @DisplayName("상태 검증 테스트")
    class StateValidationTest {

        @Test
        @DisplayName("멤버 대기 단계가 아닐 때 예외")
        void requireWaitingForMembersPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);
            session.startQuestionRevealPhase();

            // when & then
            assertThatThrownBy(session::requireWaitingForMembersPhase)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("멤버 대기 단계가 아닙니다");
        }

        @Test
        @DisplayName("투표 단계가 아닐 때 예외")
        void requireVotingPhaseTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when & then
            assertThatThrownBy(session::requireVotingPhase)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("투표 단계가 아닙니다");
        }

        @Test
        @DisplayName("잘못된 단계에서 투표 시작 시 예외")
        void validateCanStartVotingFailTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when & then
            assertThatThrownBy(session::validateCanStartVoting)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("투표를 시작할 수 없는 상태입니다");
        }

        @Test
        @DisplayName("게임이 완료되지 않았을 때 예외")
        void requireGameCompletedTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when & then
            assertThatThrownBy(session::requireGameCompleted)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("게임이 완료되지 않았습니다");
        }
    }

    @Nested
    @DisplayName("게임 단계 확인 테스트")
    class GameStepCheckTest {

        @Test
        @DisplayName("게임 단계별 확인 메서드")
        void gameStepCheckMethodsTest() {
            // given
            BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3);

            // when & then - WAITING_FOR_MEMBERS 단계
            assertThat(session.isWaitingForMembersPhase()).isTrue();
            assertThat(session.isQuestionRevealPhase()).isFalse();

            // 상태 전환
            session.startQuestionRevealPhase();
            assertThat(session.isQuestionRevealPhase()).isTrue();
            assertThat(session.isDiscussionPhase()).isFalse();

            session.startDiscussionPhase();
            assertThat(session.isDiscussionPhase()).isTrue();
            assertThat(session.isVotingPhase()).isFalse();

            session.startVotingPhase();
            assertThat(session.isVotingPhase()).isTrue();
            assertThat(session.isResultViewPhase()).isFalse();

            session.processVote();
            session.processVote();
            session.processVote();
            session.startResultViewPhase();
            assertThat(session.isResultViewPhase()).isTrue();
            assertThat(session.isCompletedPhase()).isFalse();

            session.completeGame();
            assertThat(session.isCompletedPhase()).isTrue();
        }
    }
} 