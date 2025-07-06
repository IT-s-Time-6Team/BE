package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.domain.BalanceVoteResult;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceFinalResultResponse;
import com.team6.team6.balance.dto.BalanceMemberScoreInfo;
import com.team6.team6.balance.dto.BalanceQuestionSummary;
import com.team6.team6.balance.dto.BalanceRoundResultResponse;
import com.team6.team6.balance.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceResultService 테스트")
class BalanceResultServiceTest {

    @Mock
    private BalanceSessionService balanceSessionService;

    @Mock
    private BalanceQuestionService balanceQuestionService;

    @Mock
    private BalanceVoteService balanceVoteService;

    @Mock
    private BalanceScoreService balanceScoreService;

    @Mock
    private BalanceMessagePublisher messagePublisher;

    @Mock
    private BalanceRevealService balanceRevealService;

    @Mock
    private BalanceVoteRepository balanceVoteRepository;

    @InjectMocks
    private BalanceResultService balanceResultService;

    @Test
    @DisplayName("최신 투표 결과 조회 - 정상 케이스")
    void getLatestVotingResult_Success_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase(); // currentQuestionIndex = 0
        
        BalanceSessionQuestion question = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        BalanceVoteResult voteResult = new BalanceVoteResult(2L, 1L, BalanceChoice.A, false);
        
        BalanceVote userVote = BalanceVote.create(roomId, memberName, 1L, BalanceChoice.A, 0, 1L);
        List<BalanceVote> userVotes = List.of(userVote);
        
        BalanceMemberScoreInfo memberScore = BalanceMemberScoreInfo.builder()
                .memberName(memberName)
                .currentScore(1)
                .rank(1)
                .build();
        
        List<BalanceMemberScoreInfo> allScores = List.of(memberScore);

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceVoteService.getRoundVoteResult(roomId, 0)).thenReturn(voteResult);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, 0)).thenReturn(question);
        when(balanceVoteRepository.findByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0)).thenReturn(userVotes);
        when(balanceScoreService.getAllMemberScores(roomId)).thenReturn(allScores);

        // when
        BalanceRoundResultResponse result = balanceResultService.getLatestVotingResult(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.myChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.choiceACount()).isEqualTo(2);
            softly.assertThat(result.choiceBCount()).isEqualTo(1);
            softly.assertThat(result.choiceAPercentage()).isCloseTo(66.67, within(0.01));
            softly.assertThat(result.choiceBPercentage()).isCloseTo(33.33, within(0.01));
            softly.assertThat(result.majorityChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.isTie()).isFalse();
            softly.assertThat(result.scoreChange()).isEqualTo(1); // 다수파 선택
            softly.assertThat(result.currentScore()).isEqualTo(1);
            softly.assertThat(result.currentRank()).isEqualTo(1);
            softly.assertThat(result.currentRound()).isEqualTo(1);
            softly.assertThat(result.allMemberScores()).hasSize(1);
        });
    }

    @Test
    @DisplayName("최신 투표 결과 조회 - 동점인 경우")
    void getLatestVotingResult_Tie_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 2, 2);
        session.startQuestionRevealPhase(); // currentQuestionIndex = 0
        
        BalanceSessionQuestion question = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        BalanceVoteResult voteResult = new BalanceVoteResult(1L, 1L, null, true); // 동점
        
        BalanceVote userVote = BalanceVote.create(roomId, memberName, 1L, BalanceChoice.A, 0, 1L);
        List<BalanceVote> userVotes = List.of(userVote);
        
        BalanceMemberScoreInfo memberScore = BalanceMemberScoreInfo.builder()
                .memberName(memberName)
                .currentScore(0)
                .rank(1)
                .build();
        
        List<BalanceMemberScoreInfo> allScores = List.of(memberScore);

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceVoteService.getRoundVoteResult(roomId, 0)).thenReturn(voteResult);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, 0)).thenReturn(question);
        when(balanceVoteRepository.findByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0)).thenReturn(userVotes);
        when(balanceScoreService.getAllMemberScores(roomId)).thenReturn(allScores);

        // when
        BalanceRoundResultResponse result = balanceResultService.getLatestVotingResult(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.myChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.choiceACount()).isEqualTo(1);
            softly.assertThat(result.choiceBCount()).isEqualTo(1);
            softly.assertThat(result.choiceAPercentage()).isCloseTo(50.0, within(0.01));
            softly.assertThat(result.choiceBPercentage()).isCloseTo(50.0, within(0.01));
            softly.assertThat(result.majorityChoice()).isNull();
            softly.assertThat(result.isTie()).isTrue();
            softly.assertThat(result.scoreChange()).isZero(); // 동점이므로 점수 변화 없음
            softly.assertThat(result.currentScore()).isZero();
            softly.assertThat(result.currentRank()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("세션 최종 결과 조회 - 정상 케이스")
    void getSessionResults_Success_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 2);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        
        // 모든 멤버가 투표 완료
        session.processVote(); // 1명
        session.processVote(); // 2명
        session.processVote(); // 3명 (모든 멤버 투표 완료)
        
        session.startResultViewPhase();
        session.completeGame(); // 게임 완료
        
        BalanceMemberScoreInfo memberScore = BalanceMemberScoreInfo.builder()
                .memberName(memberName)
                .currentScore(2)
                .rank(1)
                .build();
        
        BalanceMemberScoreInfo winnerScore = BalanceMemberScoreInfo.builder()
                .memberName("winner")
                .currentScore(3)
                .rank(1)
                .build();
        
        List<BalanceMemberScoreInfo> allScores = List.of(winnerScore, memberScore);

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceScoreService.getAllMemberScores(roomId)).thenReturn(allScores);

        // when
        BalanceFinalResultResponse result = balanceResultService.getSessionResults(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.memberName()).isEqualTo(memberName);
            softly.assertThat(result.finalScore()).isEqualTo(2);
            softly.assertThat(result.finalRank()).isEqualTo(1);
            softly.assertThat(result.winnerNickname()).isEqualTo("winner");
            softly.assertThat(result.mostBalancedQuestions()).isNotNull().isInstanceOf(List.class);
            softly.assertThat(result.mostUnanimousQuestions()).isNotNull().isInstanceOf(List.class);
        });
    }

    @Test
    @DisplayName("세션 최종 결과 조회 - 게임 완료되지 않은 경우 예외")
    void getSessionResults_GameNotCompleted_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 2);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        // 게임 완료하지 않음

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceResultService.getSessionResults(roomId, memberName))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("세션 최종 결과 조회 - 존재하지 않는 멤버")
    void getSessionResults_MemberNotFound_Test() {
        // given
        Long roomId = 1L;
        String memberName = "nonexistent";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 2);
        session.completeGame(); // 게임 완료
        
        BalanceMemberScoreInfo otherMemberScore = BalanceMemberScoreInfo.builder()
                .memberName("otherUser")
                .currentScore(1)
                .rank(1)
                .build();
        
        List<BalanceMemberScoreInfo> allScores = List.of(otherMemberScore);

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceScoreService.getAllMemberScores(roomId)).thenReturn(allScores);

        // when & then
        assertThatThrownBy(() -> balanceResultService.getSessionResults(roomId, memberName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버를 찾을 수 없습니다: " + memberName);
    }

    @Test
    @DisplayName("게임 준비 완료 처리 - 모든 멤버 준비 완료, 다음 문제로 이동")
    void processGameReady_AllReadyNextQuestion_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        
        // 모든 멤버가 투표 완료
        session.processVote(); // 1명
        session.processVote(); // 2명
        session.processVote(); // 3명 (모든 멤버 투표 완료)
        
        session.startResultViewPhase();
        
        // 다른 멤버들이 먼저 결과 확인 완료 (processGameReady에서 한 번 더 호출됨)
        session.processResultViewReady(); // 1명
        session.processResultViewReady(); // 2명
        // processGameReady에서 3번째 호출로 모든 멤버 준비 완료
        
        // 2번째 문제 준비 (현재 인덱스 0, 다음 인덱스 1)
        assertThat(session.getCurrentQuestionIndex()).isEqualTo(0);
        assertThat(session.isLastQuestionIndex()).isFalse();

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceResultService.processGameReady(roomKey, roomId, memberName);

        // then
        verify(balanceRevealService).startQuestionReveal(roomKey, roomId);
        assertThat(session.getCurrentQuestionIndex()).isEqualTo(1); // 다음 문제로 이동
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.QUESTION_REVEAL);
    }

    @Test
    @DisplayName("게임 준비 완료 처리 - 모든 멤버 준비 완료, 게임 완료")
    void processGameReady_AllReadyGameCompleted_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 1); // 1라운드만
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        
        // 모든 멤버가 투표 완료
        session.processVote(); // 1명
        session.processVote(); // 2명
        session.processVote(); // 3명 (모든 멤버 투표 완료)
        
        session.startResultViewPhase();
        
        // 다른 멤버들이 먼저 결과 확인 완료 (processGameReady에서 한 번 더 호출됨)
        session.processResultViewReady(); // 1명
        session.processResultViewReady(); // 2명
        // processGameReady에서 3번째 호출로 모든 멤버 준비 완료
        
        // 마지막 문제 (현재 인덱스 0, 총 문제 1개)
        assertThat(session.getCurrentQuestionIndex()).isEqualTo(0);
        assertThat(session.isLastQuestionIndex()).isTrue();

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceResultService.processGameReady(roomKey, roomId, memberName);

        // then
        verify(messagePublisher).notifyBalanceGameCompleted(roomKey);
        verify(balanceRevealService, never()).startQuestionReveal(anyString(), anyLong());
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.COMPLETED);
    }

    @Test
    @DisplayName("게임 준비 완료 처리 - 아직 대기 중인 멤버 있음")
    void processGameReady_StillWaiting_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3); // 4명 중 아직 일부만 준비
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        
        // 모든 멤버가 투표 완료
        session.processVote(); // 1명
        session.processVote(); // 2명
        session.processVote(); // 3명
        session.processVote(); // 4명 (모든 멤버 투표 완료)
        
        session.startResultViewPhase();
        
        // 아직 모든 멤버가 준비되지 않음
        session.processResultViewReady(); // 1명
        session.processResultViewReady(); // 2명
        // 3번째 호출은 processGameReady 내에서 처리됨 (아직 4명 중 3명만 준비)

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceResultService.processGameReady(roomKey, roomId, memberName);

        // then
        verify(messagePublisher).notifyBalanceGameReady(eq(roomKey), eq(3), eq(4));
        verify(balanceRevealService, never()).startQuestionReveal(anyString(), anyLong());
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.RESULT_VIEW);
    }

    @Test
    @DisplayName("게임 준비 완료 처리 - 이미 게임 완료된 경우")
    void processGameReady_AlreadyCompleted_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 2);
        session.completeGame(); // 이미 게임 완료

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceResultService.processGameReady(roomKey, roomId, memberName);

        // then
        verify(messagePublisher, never()).notifyBalanceGameCompleted(anyString());
        verify(balanceRevealService, never()).startQuestionReveal(anyString(), anyLong());
        verify(messagePublisher, never()).notifyBalanceGameReady(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("게임 준비 완료 처리 - 잘못된 단계에서 호출 시 예외")
    void processGameReady_InvalidPhase_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 2);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        // RESULT_VIEW 단계로 이동하지 않음

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceResultService.processGameReady(roomKey, roomId, memberName))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("사용자 투표 없을 때 기본값 반환")
    void getLatestVotingResult_NoUserVote_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        
        BalanceSessionQuestion question = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        BalanceVoteResult voteResult = new BalanceVoteResult(2L, 1L, BalanceChoice.A, false);
        
        // 사용자 투표가 없음
        List<BalanceVote> emptyVotes = List.of();
        
        BalanceMemberScoreInfo memberScore = BalanceMemberScoreInfo.builder()
                .memberName(memberName)
                .currentScore(0)
                .rank(1)
                .build();
        
        List<BalanceMemberScoreInfo> allScores = List.of(memberScore);

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceVoteService.getRoundVoteResult(roomId, 0)).thenReturn(voteResult);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, 0)).thenReturn(question);
        when(balanceVoteRepository.findByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0)).thenReturn(emptyVotes);
        when(balanceScoreService.getAllMemberScores(roomId)).thenReturn(allScores);

        // when
        BalanceRoundResultResponse result = balanceResultService.getLatestVotingResult(roomId, memberName);

        // then
        assertThat(result.myChoice()).isEqualTo(BalanceChoice.A); // 기본값
        assertThat(result.scoreChange()).isEqualTo(1); // 기본값(A)이 다수파와 일치
    }

    private BalanceSessionQuestion createBalanceSessionQuestion(Long id, Long roomId, int displayOrder, String questionA, String questionB) {
        return BalanceSessionQuestion.builder()
                .roomId(roomId)
                .balanceQuestionId(id)
                .questionA(questionA)
                .questionB(questionB)
                .displayOrder(displayOrder)
                .build();
    }
} 