package com.team6.team6.tmi.service;

import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.domain.repository.TmiVoteRepository;
import com.team6.team6.tmi.dto.TmiVoteServiceReq;
import com.team6.team6.tmi.dto.TmiVotingPersonalResult;
import com.team6.team6.tmi.dto.TmiVotingStartResponse;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.entity.TmiSession;
import com.team6.team6.tmi.entity.TmiSubmission;
import com.team6.team6.tmi.entity.TmiVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TmiVoteServiceTest {

    @Autowired
    private TmiVoteService tmiVoteService;

    @Autowired
    private TmiSessionService tmiSessionService;

    @Autowired
    private TmiSessionRepository tmiSessionRepository;

    @Autowired
    private TmiSubmissionRepository tmiSubmissionRepository;

    @Autowired
    private TmiVoteRepository tmiVoteRepository;

    @MockitoBean
    private TmiMessagePublisher messagePublisher;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 투표_시작_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 3);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();

        TmiSubmission submission1 = createTmiSubmission(1L, "member1", "TMI1");
        TmiSubmission submission2 = createTmiSubmission(1L, "member2", "TMI2");
        TmiSubmission submission3 = createTmiSubmission(1L, "member3", "TMI3");

        tmiSessionRepository.save(session);
        tmiSubmissionRepository.saveAll(List.of(submission1, submission2, submission3));

        // when
        tmiVoteService.startVotingPhase("room1", 1L);

        // then
        TmiSession updatedSession = tmiSessionRepository.findById(session.getId()).get();
        assertSoftly(softly -> {
            softly.assertThat(updatedSession.getCurrentStep()).isEqualTo(TmiGameStep.VOTING);
            softly.assertThat(updatedSession.getCurrentVotingTmiIndex()).isEqualTo(0);
        });
        verify(messagePublisher).notifyTmiVotingStarted("room1");
    }

    @Test
    @DisplayName("투표를 제출할 수 있다")
    void 투표_제출_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();
        tmiSessionRepository.save(session);

        TmiSubmission submission = createTmiSubmission(1L, "member1", "TMI1");
        submission.setDisplayOrder(0);
        tmiSubmissionRepository.save(submission);

        TmiVoteServiceReq request = new TmiVoteServiceReq("room1", 1L, "voter1", 1L, CharacterType.BEAR, "member1");

        // when
        tmiVoteService.submitVote(request);

        // then
        List<TmiVote> votes = tmiVoteRepository.findAll();
        TmiVote vote = votes.get(0);

        assertSoftly(softly -> {
            softly.assertThat(vote.getRoomId()).isEqualTo(1L);
            softly.assertThat(vote.getVoterName()).isEqualTo("voter1");
            softly.assertThat(vote.getVotedMemberName()).isEqualTo("member1");
            softly.assertThat(vote.getVotingRound()).isEqualTo(0);
            softly.assertThat(vote.getIsCorrect()).isTrue();
        });
        verify(messagePublisher).notifyTmiVotingProgress(eq("room1"), anyInt());
    }

    @Test
    @DisplayName("현재 투표 정보를 조회할 수 있다")
    void 현재_투표_정보_조회_테스트() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        tmiSessionRepository.save(session);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();

        TmiSubmission submission1 = createTmiSubmission(1L, "member1", "TMI1");
        submission1.setDisplayOrder(0);
        TmiSubmission submission2 = createTmiSubmission(1L, "member2", "TMI2");
        submission2.setDisplayOrder(1);
        tmiSubmissionRepository.saveAll(List.of(submission1, submission2));

        // when
        TmiVotingStartResponse response = tmiVoteService.getCurrentVotingInfo(1L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.tmiContent()).isEqualTo("TMI1");
            softly.assertThat(response.round()).isEqualTo(0);
            softly.assertThat(response.members()).containsExactlyInAnyOrder("member1", "member2");
        });
    }

    @Test
    void 최신_투표_결과_조회() {
        // given
        TmiSession session = TmiSession.createInitialSession(1L, 2);
        session.incrementSubmittedTmiCount();
        session.incrementSubmittedTmiCount();
        session.startHintTime();
        session.startVotingPhase();
        tmiSessionRepository.save(session);

        TmiSubmission submission = createTmiSubmission(1L, "member2", "TMI1");
        submission.setDisplayOrder(0);
        tmiSubmissionRepository.save(submission);

        TmiVote vote1 = TmiVote.create(1L, "member1", 1L, CharacterType.BEAR, "member2", 2L, CharacterType.BEAR, submission.getId(), 0);
        vote1.changeIsCorrect("member2");

        TmiVote vote2 = TmiVote.create(1L, "member2", 2L, CharacterType.RABBIT, "member1", 1L, CharacterType.BEAR, submission.getId(), 0);
        vote2.changeIsCorrect("member1");
        tmiVoteRepository.saveAll(List.of(vote1, vote2));

        session.processVote();
        session.processVote();

        // when
        TmiVotingPersonalResult result = tmiVoteService.getLatestVotingResult(1L, "member1");

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.tmiContent()).isEqualTo("TMI1");
            softly.assertThat(result.correctAnswer()).isEqualTo("member2");
            softly.assertThat(result.myVote()).isEqualTo("member2");
            softly.assertThat(result.isCorrect()).isTrue();
            softly.assertThat(result.votingResults().get("member1")).isEqualTo(1L);
        });
    }

    private TmiSubmission createTmiSubmission(Long roomId, String memberName, String content) {
        return TmiSubmission.builder()
                .roomId(roomId)
                .memberName(memberName)
                .tmiContent(content)
                .build();
    }
}
