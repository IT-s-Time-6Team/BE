package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.domain.repository.TmiVoteRepository;
import com.team6.team6.tmi.dto.TmiSessionStatusResponse;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.entity.TmiSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmiSessionServiceTest {

    @Mock
    private TmiSubmissionRepository tmiSubmissionRepository;

    @Mock
    private TmiVoteRepository tmiVoteRepository;

    @Mock
    private TmiSessionRepository tmiSessionRepository;

    @InjectMocks
    private TmiSessionService tmiSessionService;

    @Test
    void TMI_수집_단계_제출_완료시_게임_상태_조회_테스트() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        TmiSession session = TmiSession.createInitialSession(roomId, 5);

        when(tmiSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));
        when(tmiSubmissionRepository.existsByRoomIdAndMemberName(roomId, memberName)).thenReturn(true);

        // when
        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(TmiGameStep.COLLECTING_TMI);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(0);
        });
    }

    @Test
    void TMI_수집_단계_제출_미완료시_게임_상태_조회_테스트() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        TmiSession session = TmiSession.createInitialSession(roomId, 5);

        when(tmiSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));
        when(tmiSubmissionRepository.existsByRoomIdAndMemberName(roomId, memberName)).thenReturn(false);

        // when
        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(TmiGameStep.COLLECTING_TMI);
            softly.assertThat(response.hasUserSubmitted()).isFalse();
            softly.assertThat(response.progress()).isEqualTo(0);
        });
    }

    @Test
    @DisplayName("TMI 게임 상태 조회 - 투표 단계, 투표 미완료")
    void 투표_미완료시_TMI_게임_상태_조회_테스트() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        TmiSession session = TmiSession.createInitialSession(roomId, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();

        when(tmiSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));
        when(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0)).thenReturn(false);

        // when
        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(TmiGameStep.VOTING);
            softly.assertThat(response.hasUserSubmitted()).isFalse();
            softly.assertThat(response.progress()).isEqualTo(0);
        });
    }

    @Test
    void 투표_완료시_TMI_게임_상태_조회_테스트() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        TmiSession session = TmiSession.createInitialSession(roomId, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();

        when(tmiSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));
        when(tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(roomId, memberName, 0)).thenReturn(true);

        // when
        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(TmiGameStep.VOTING);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(0);
        });
    }

    @Test
    void TMI_게임_종료시_게임_상태_조회_테스트() {
        // given
        Long roomId = 1L;
        String memberName = "testUser";
        TmiSession session = TmiSession.createInitialSession(roomId, 1);
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();
        session.processVote();

        when(tmiSessionRepository.findByRoomIdWithLock(roomId)).thenReturn(Optional.of(session));

        // when
        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.currentStep()).isEqualTo(TmiGameStep.COMPLETED);
            softly.assertThat(response.hasUserSubmitted()).isTrue();
            softly.assertThat(response.progress()).isEqualTo(100);
        });
    }
}
