package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.repository.BalanceSessionRepository;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceSessionStatusResponse;
import com.team6.team6.balance.entity.BalanceGameStep;
import com.team6.team6.balance.entity.BalanceSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceSessionService 테스트")
class BalanceSessionServiceTest {

    @Mock
    private BalanceSessionRepository balanceSessionRepository;

    @Mock
    private BalanceVoteRepository balanceVoteRepository;

    @InjectMocks
    private BalanceSessionService balanceSessionService;

    @Test
    @DisplayName("게임 상태 조회 - 멤버 대기 단계")
    void getSessionStatus_WaitingForMembers_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(session));

        // when
        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(BalanceGameStep.WAITING_FOR_MEMBERS);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(100);
        });
    }

    @Test
    @DisplayName("게임 상태 조회 - 투표 단계, 투표 완료")
    void getSessionStatus_Voting_Completed_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        session.processVote();
        session.processVote();
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(session));
        when(balanceVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0))
                .thenReturn(true);

        // when
        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(BalanceGameStep.VOTING);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(50); // 2/4 * 100 = 50%
        });
    }

    @Test
    @DisplayName("게임 상태 조회 - 투표 단계, 투표 미완료")
    void getSessionStatus_Voting_NotCompleted_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(session));
        when(balanceVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0))
                .thenReturn(false);

        // when
        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(BalanceGameStep.VOTING);
            softly.assertThat(response.hasUserSubmitted()).isFalse();
            softly.assertThat(response.progress()).isEqualTo(0); // 0/4 * 100 = 0%
        });
    }

    @Test
    @DisplayName("게임 상태 조회 - 결과 확인 단계")
    void getSessionStatus_ResultView_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        session.startDiscussionPhase();
        session.startVotingPhase();
        session.processVote();
        session.processVote();
        session.processVote();
        session.startResultViewPhase();
        session.processResultViewReady(); // 1/3 확인 완료
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(session));

        // when
        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(BalanceGameStep.RESULT_VIEW);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.waitingForOthers()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(33); // 1/3 * 100 = 33% (소수점 버림)
        });
    }

    @Test
    @DisplayName("게임 상태 조회 - 게임 완료")
    void getSessionStatus_Completed_Test() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.completeGame();
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(session));

        // when
        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(BalanceGameStep.COMPLETED);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.waitingForOthers()).isFalse();
            softly.assertThat(response.progress()).isEqualTo(100);
        });
    }

    @Test
    @DisplayName("존재하지 않는 방 ID로 게임 상태 조회 시 예외")
    void getSessionStatus_RoomNotFound_Test() {
        // given
        Long roomId = 999L;
        String memberName = "testUser";
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> balanceSessionService.getSessionStatus(roomId, memberName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("밸런스 게임 세션을 찾을 수 없습니다: " + roomId);
    }

    @Test
    @DisplayName("밸런스 게임 세션 생성")
    void createBalanceGameSession_Test() {
        // given
        Long roomId = 1L;
        int totalMembers = 4;
        int totalQuestions = 3;
        
        // when
        balanceSessionService.createBalanceGameSession(roomId, totalMembers, totalQuestions);

        // then
        verify(balanceSessionRepository).save(any(BalanceSession.class));
    }

    @Test
    @DisplayName("방 ID로 세션 조회")
    void findSessionByRoomId_Test() {
        // given
        Long roomId = 1L;
        BalanceSession expectedSession = BalanceSession.createInitialSession(roomId, 4, 3);
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.of(expectedSession));

        // when
        BalanceSession result = balanceSessionService.findSessionByRoomId(roomId);

        // then
        assertThat(result).isEqualTo(expectedSession);
        verify(balanceSessionRepository).findByRoomId(roomId);
    }

    @Test
    @DisplayName("존재하지 않는 세션 조회 시 예외")
    void findSessionByRoomId_NotFound_Test() {
        // given
        Long roomId = 999L;
        
        when(balanceSessionRepository.findByRoomId(roomId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> balanceSessionService.findSessionByRoomId(roomId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("밸런스 게임 세션을 찾을 수 없습니다: " + roomId);
    }

    @Test
    @DisplayName("잠금과 함께 세션 조회")
    void findSessionByRoomIdWithLock_Test() {
        // given
        Long roomId = 1L;
        BalanceSession expectedSession = BalanceSession.createInitialSession(roomId, 4, 3);
        
        when(balanceSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(expectedSession));

        // when
        BalanceSession result = balanceSessionService.findSessionByRoomIdWithLock(roomId);

        // then
        assertThat(result).isEqualTo(expectedSession);
        verify(balanceSessionRepository).findByRoomIdWithLock(roomId);
    }

    @Test
    @DisplayName("멤버 입장 체크 및 시작 - 모든 멤버 입장 완료")
    void checkMemberJoinAndStartIfReady_AllJoined_Test() {
        // given
        Long roomId = 1L;
        int currentMemberCount = 4;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        
        when(balanceSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));

        // when
        boolean result = balanceSessionService.checkMemberJoinAndStartIfReady(roomId, currentMemberCount);

        // then
        assertThat(result).isTrue();
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.QUESTION_REVEAL);
    }

    @Test
    @DisplayName("멤버 입장 체크 및 시작 - 아직 입장 미완료")
    void checkMemberJoinAndStartIfReady_NotAllJoined_Test() {
        // given
        Long roomId = 1L;
        int currentMemberCount = 2;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        
        when(balanceSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));

        // when
        boolean result = balanceSessionService.checkMemberJoinAndStartIfReady(roomId, currentMemberCount);

        // then
        assertThat(result).isFalse();
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.WAITING_FOR_MEMBERS);
    }

    @Test
    @DisplayName("멤버 입장 체크 및 시작 - 이미 시작된 게임")
    void checkMemberJoinAndStartIfReady_AlreadyStarted_Test() {
        // given
        Long roomId = 1L;
        int currentMemberCount = 4;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase(); // 이미 시작됨
        
        when(balanceSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));

        // when
        boolean result = balanceSessionService.checkMemberJoinAndStartIfReady(roomId, currentMemberCount);

        // then
        assertThat(result).isFalse();
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.QUESTION_REVEAL);
    }
} 