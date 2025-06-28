package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.domain.repository.TmiVoteRepository;
import com.team6.team6.tmi.dto.TmiSessionResultResponse;
import com.team6.team6.tmi.dto.TmiSessionStatusResponse;
import com.team6.team6.tmi.dto.TopVoter;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.entity.TmiSession;
import com.team6.team6.tmi.entity.TmiSubmission;
import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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

    @Test
    void TMI_게임_최종_결과_조회_테스트() {
        // given
        // 세션 생성 및 COMPLETED 상태로 설정
        TmiSession session = TmiSession.createInitialSession(1L, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();

        // COMPLETED 상태로 설정
        while (session.getCurrentStep() != TmiGameStep.COMPLETED) {
            session.processVote();
        }

        when(tmiSessionRepository.findByRoomId(1L)).thenReturn(Optional.of(session));

        // TMI 제출 데이터 생성
        TmiSubmission submission1 = createTmiSubmission(1L, "member1", "TMI1");
        submission1.setDisplayOrder(0);
        TmiSubmission submission2 = createTmiSubmission(1L, "member2", "TMI2");
        submission2.setDisplayOrder(1);
        TmiSubmission submission3 = createTmiSubmission(1L, "member3", "TMI3");
        submission3.setDisplayOrder(2);
        when(tmiSubmissionRepository.findAllByRoomId(1L))
                .thenReturn(List.of(submission1, submission2, submission3));

        // 투표 데이터 생성
        TmiVote vote1 = TmiVote.create(1L, "member1", "member2", submission2.getId(), 1);
        vote1.changeIsCorrect("member2"); // 맞춤
        TmiVote vote2 = TmiVote.create(1L, "member2", "member1", submission1.getId(), 0);
        vote2.changeIsCorrect("member1"); // 맞춤
        TmiVote vote3 = TmiVote.create(1L, "member3", "member1", submission1.getId(), 0);
        vote3.changeIsCorrect("member1"); // 맞춤
        TmiVote vote4 = TmiVote.create(1L, "member1", "member1", submission3.getId(), 2);
        vote4.changeIsCorrect("member3"); // 틀림 (member3의 TMI를 틀림)
        TmiVote vote5 = TmiVote.create(1L, "member2", "member1", submission3.getId(), 2);
        vote5.changeIsCorrect("member3"); // 틀림 (member3의 TMI를 틀림)
        when(tmiVoteRepository.findAllByRoomId(1L))
                .thenReturn(List.of(vote1, vote2, vote3, vote4, vote5));

        // when
        TmiSessionResultResponse result = tmiSessionService.getSessionResults(1L, "member1");

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.correctCount()).isEqualTo(1); // member1은 1개 맞춤
            softly.assertThat(result.incorrectCount()).isEqualTo(1); // member1은 1개 틀림

            // member1, member2, member3 모두 1개씩 맞췄으므로 모두 포함되어야 함
            softly.assertThat(result.topVoters()).hasSize(3);
            softly.assertThat(result.topVoters().stream()
                            .map(TopVoter::memberName)
                            .collect(Collectors.toList()))
                    .containsExactlyInAnyOrder("member1", "member2", "member3");

            // member3의 TMI가 가장 많이 틀림
            softly.assertThat(result.mostIncorrectTmis()).hasSize(1);
            softly.assertThat(result.mostIncorrectTmis().get(0).tmiContent()).isEqualTo("TMI3");
            softly.assertThat(result.mostIncorrectTmis().get(0).incorrectVoteCount()).isEqualTo(2);
        });
    }

    @Test
    void 미완료_게임_결과_조회_예외_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime(); // HINT 상태로 설정

        when(tmiSessionRepository.findByRoomId(1L)).thenReturn(Optional.of(session));

        // when & then
        assertThatThrownBy(() -> tmiSessionService.getSessionResults(1L, "member1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TMI 게임이 아직 완료되지 않았습니다");
    }

    private TmiSubmission createTmiSubmission(long roomId, String member, String content) {
        return TmiSubmission.builder()
                .roomId(roomId)
                .memberName(member)
                .tmiContent(content)
                .build();
    }
}
