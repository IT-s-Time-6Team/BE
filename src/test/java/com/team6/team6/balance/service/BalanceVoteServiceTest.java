package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.domain.BalanceVoteResult;
import com.team6.team6.balance.domain.VoteStatus;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceVoteServiceReq;
import com.team6.team6.balance.dto.BalanceVotingStartResponse;
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
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceVoteService 테스트")
class BalanceVoteServiceTest {

    @Mock
    private BalanceSessionService balanceSessionService;

    @Mock
    private BalanceQuestionService balanceQuestionService;

    @Mock
    private BalanceVoteRepository balanceVoteRepository;

    @Mock
    private BalanceMessagePublisher messagePublisher;

    @Mock
    private BalanceScoreService balanceScoreService;

    @InjectMocks
    private BalanceVoteService balanceVoteService;

    @Test
    @DisplayName("투표 단계 시작 - 정상 케이스")
    void startVotingPhase_Success_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceVoteService.startVotingPhase(roomKey, roomId);

        // then
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.VOTING);
        verify(messagePublisher).notifyBalanceVotingStarted(roomKey);
    }

    @Test
    @DisplayName("투표 단계 시작 - 잘못된 단계에서 시작 시 예외")
    void startVotingPhase_InvalidPhase_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        // 아직 WAITING_FOR_MEMBERS 단계

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceVoteService.startVotingPhase(roomKey, roomId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("투표 제출 - 라운드 진행 중")
    void submitVote_InProgress_Test() {
        // given
        BalanceVoteServiceReq req = BalanceVoteServiceReq.builder()
                .roomId(1L)
                .roomKey("ROOM123")
                .memberId(1L)
                .memberName("testUser")
                .selectedChoice(BalanceChoice.A)
                .build();

        BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();

        BalanceSessionQuestion currentQuestion = createBalanceSessionQuestion(1L, 1L, 0, "치킨", "피자");

        when(balanceSessionService.findSessionByRoomIdWithLock(1L)).thenReturn(session);
        when(balanceVoteRepository.existsByRoomIdAndMemberIdAndVotingRound(1L, 1L, 0)).thenReturn(false);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(1L, 0)).thenReturn(currentQuestion);

        // session.processVote()는 실제 구현에서 VoteStatus를 반환
        // 하지만 모킹하기 어려우므로 session의 상태를 미리 설정
        session.processVote(); // 1번째 투표

        // when
        balanceVoteService.submitVote(req);

        // then
        verify(balanceVoteRepository).save(any(BalanceVote.class));
        verify(messagePublisher).notifyBalanceVotingProgress(eq("ROOM123"), anyInt());
        verify(balanceScoreService, never()).calculateAndUpdateScores(anyLong(), anyInt());
    }

    @Test
    @DisplayName("투표 제출 - 라운드 완료")
    void submitVote_RoundCompleted_Test() {
        // given
        BalanceVoteServiceReq req = BalanceVoteServiceReq.builder()
                .roomId(1L)
                .roomKey("ROOM123")
                .memberId(1L)
                .memberName("testUser")
                .selectedChoice(BalanceChoice.A)
                .build();

        BalanceSession session = BalanceSession.createInitialSession(1L, 3, 3); // 3명으로 설정
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        session.processVote(); // 1번째 투표
        session.processVote(); // 2번째 투표
        // 3번째 투표는 submitVote 내에서 처리됨

        BalanceSessionQuestion currentQuestion = createBalanceSessionQuestion(1L, 1L, 0, "치킨", "피자");

        when(balanceSessionService.findSessionByRoomIdWithLock(1L)).thenReturn(session);
        when(balanceVoteRepository.existsByRoomIdAndMemberIdAndVotingRound(1L, 1L, 0)).thenReturn(false);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(1L, 0)).thenReturn(currentQuestion);

        // when
        balanceVoteService.submitVote(req);

        // then
        verify(balanceVoteRepository).save(any(BalanceVote.class));
        verify(balanceScoreService).calculateAndUpdateScores(1L, 0);
        verify(messagePublisher).notifyBalanceRoundCompleted("ROOM123", 0);
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.RESULT_VIEW);
    }

    @Test
    @DisplayName("투표 제출 - 모든 라운드 완료")
    void submitVote_AllCompleted_Test() {
        // given
        BalanceVoteServiceReq req = BalanceVoteServiceReq.builder()
                .roomId(1L)
                .roomKey("ROOM123")
                .memberId(1L)
                .memberName("testUser")
                .selectedChoice(BalanceChoice.A)
                .build();

        BalanceSession session = BalanceSession.createInitialSession(1L, 2, 1); // 2명, 1라운드
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        session.processVote(); // 1번째 투표
        // 2번째 투표는 submitVote 내에서 처리됨

        BalanceSessionQuestion currentQuestion = createBalanceSessionQuestion(1L, 1L, 0, "치킨", "피자");

        when(balanceSessionService.findSessionByRoomIdWithLock(1L)).thenReturn(session);
        when(balanceVoteRepository.existsByRoomIdAndMemberIdAndVotingRound(1L, 1L, 0)).thenReturn(false);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(1L, 0)).thenReturn(currentQuestion);

        // when
        balanceVoteService.submitVote(req);

        // then
        verify(balanceVoteRepository).save(any(BalanceVote.class));
        verify(balanceScoreService).calculateAndUpdateScores(1L, 0);
        verify(messagePublisher).notifyBalanceGameCompleted("ROOM123");
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.COMPLETED);
    }

    @Test
    @DisplayName("투표 제출 - 중복 투표 시 예외")
    void submitVote_DuplicateVote_Test() {
        // given
        BalanceVoteServiceReq req = BalanceVoteServiceReq.builder()
                .roomId(1L)
                .roomKey("ROOM123")
                .memberId(1L)
                .memberName("testUser")
                .selectedChoice(BalanceChoice.A)
                .build();

        BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();

        when(balanceSessionService.findSessionByRoomIdWithLock(1L)).thenReturn(session);
        when(balanceVoteRepository.existsByRoomIdAndMemberIdAndVotingRound(1L, 1L, 0)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> balanceVoteService.submitVote(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 이 라운드에 투표했습니다");

        verify(balanceVoteRepository, never()).save(any(BalanceVote.class));
    }

    @Test
    @DisplayName("투표 제출 - 투표 단계가 아닐 때 예외")
    void submitVote_NotVotingPhase_Test() {
        // given
        BalanceVoteServiceReq req = BalanceVoteServiceReq.builder()
                .roomId(1L)
                .roomKey("ROOM123")
                .memberId(1L)
                .memberName("testUser")
                .selectedChoice(BalanceChoice.A)
                .build();

        BalanceSession session = BalanceSession.createInitialSession(1L, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        // 투표 단계 시작하지 않음

        when(balanceSessionService.findSessionByRoomIdWithLock(1L)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceVoteService.submitVote(req))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("라운드 투표 결과 조회")
    void getRoundVoteResult_Test() {
        // given
        Long roomId = 1L;
        int round = 0;

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.A, round, 1L);
        BalanceVote vote3 = BalanceVote.create(roomId, "user3", 3L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2, vote3);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        BalanceVoteResult result = balanceVoteService.getRoundVoteResult(roomId, round);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(2);
            softly.assertThat(result.choiceBCount()).isEqualTo(1);
            softly.assertThat(result.majorityChoice()).isEqualTo(BalanceChoice.A);
            softly.assertThat(result.isTie()).isFalse();
        });
    }

    @Test
    @DisplayName("라운드 투표 결과 조회 - 동점인 경우")
    void getRoundVoteResult_Tie_Test() {
        // given
        Long roomId = 1L;
        int round = 0;

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        BalanceVoteResult result = balanceVoteService.getRoundVoteResult(roomId, round);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isEqualTo(1);
            softly.assertThat(result.choiceBCount()).isEqualTo(1);
            softly.assertThat(result.majorityChoice()).isNull();
            softly.assertThat(result.isTie()).isTrue();
        });
    }

    @Test
    @DisplayName("현재 투표 정보 조회")
    void getCurrentVotingInfo_Test() {
        // given
        Long roomId = 1L;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase(); // currentQuestionIndex = 0

        BalanceSessionQuestion currentQuestion = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");

        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, 0)).thenReturn(currentQuestion);

        // when
        BalanceVotingStartResponse response = balanceVoteService.getCurrentVotingInfo(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.questionA()).isEqualTo("치킨");
            softly.assertThat(response.questionB()).isEqualTo("피자");
        });
    }

    @Test
    @DisplayName("빈 투표 목록으로 결과 조회")
    void getRoundVoteResult_EmptyVotes_Test() {
        // given
        Long roomId = 1L;
        int round = 0;
        List<BalanceVote> emptyVotes = List.of();

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(emptyVotes);

        // when
        BalanceVoteResult result = balanceVoteService.getRoundVoteResult(roomId, round);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.choiceACount()).isZero();
            softly.assertThat(result.choiceBCount()).isZero();
            softly.assertThat(result.majorityChoice()).isNull();
            softly.assertThat(result.isTie()).isTrue(); // 0:0도 동점으로 처리
        });
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