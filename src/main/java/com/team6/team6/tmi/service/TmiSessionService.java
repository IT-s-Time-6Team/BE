package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiSubmissions;
import com.team6.team6.tmi.domain.TmiVotes;
import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.domain.repository.TmiVoteRepository;
import com.team6.team6.tmi.dto.*;
import com.team6.team6.tmi.entity.TmiGameStep;
import com.team6.team6.tmi.entity.TmiSession;
import com.team6.team6.tmi.entity.TmiSubmission;
import com.team6.team6.tmi.entity.TmiVote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TmiSessionService {

    private final TmiSubmissionRepository tmiSubmissionRepository;
    private final TmiVoteRepository tmiVoteRepository;
    private final TmiSessionRepository tmiSessionRepository;

    public TmiSessionStatusResponse getSessionStatus(Long roomId, String memberName) {
        TmiSession session = findTmiSession(roomId);
        TmiGameStep currentStep = session.getCurrentStep();
        boolean hasUserSubmitted = false;
        int progress = 100;

        // 현재 단계에 따른 처리
        switch (currentStep) {
            case COLLECTING_TMI -> {
                // TMI 수집 단계: 유저가 TMI를 제출했는지 확인
                hasUserSubmitted = tmiSubmissionRepository.existsByRoomIdAndMemberName(roomId, memberName);
                progress = session.calculateCollectionProgress();
            }
            case HINT -> {
                // 힌트 단계: TMI는 이미 수집 완료된 상태
                hasUserSubmitted = true;
            }
            case VOTING -> {
                // 투표 단계: 현재 라운드에 유저가 투표했는지 확인
                Integer currentVotingTmiIndex = session.getCurrentVotingTmiIndex();
                hasUserSubmitted = tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(
                        roomId, memberName, currentVotingTmiIndex);
                progress = session.getCurrentRoundVotingProgress();
            }
            case COMPLETED -> {
                // 게임 완료 단계
                hasUserSubmitted = true;
            }
        }

        return TmiSessionStatusResponse.builder()
                .currentStep(currentStep)
                .hasUserSubmitted(hasUserSubmitted)
                .progress(progress)
                .build();
    }

    @Transactional
    public void createTmiGameSession(Long roomId, int totalMembers) {
        TmiSession session = TmiSession.createInitialSession(roomId, totalMembers);
        tmiSessionRepository.save(session);
        log.info("TMI 게임 세션 생성: roomId={}, totalMembers={}", roomId, totalMembers);
    }

    public TmiSession findTmiSession(Long roomId) {
        return tmiSessionRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> new IllegalStateException("TMI 게임 세션을 찾을 수 없습니다: " + roomId));
    }

    public TmiSessionResultResponse getSessionResults(Long roomId, String memberName) {
        // 1. 세션 가져오기 및 완료 상태 검증
        TmiSession session = tmiSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalStateException("TMI 게임 세션을 찾을 수 없습니다: " + roomId));
        session.validateCompleted();

        // 2. 모든 제출과 투표 가져오기
        List<TmiSubmission> submissionList = tmiSubmissionRepository.findAllByRoomId(roomId);
        List<TmiVote> voteList = tmiVoteRepository.findAllByRoomId(roomId);

        // 3. 일급 컬렉션 생성
        TmiSubmissions submissions = TmiSubmissions.from(submissionList);
        TmiVotes votes = TmiVotes.from(voteList);

        // 4. 결과 계산
        // 4.1 특정 사용자의 맞춘/틀린 수
        VoteResult userVoteResult = votes.calculateMemberVoteResult(memberName);

        // 4.2 가장 많이 맞춘 사람들
        List<TopVoter> topVoters = votes.getTopVoters();

        // 4.3 가장 많은 사람이 틀린 TMI
        List<MostIncorrectTmi> mostIncorrectTmis =
                submissions.findMostIncorrectTmis(votes);

        // 5. 응답 생성
        return new TmiSessionResultResponse(
                userVoteResult.correctCount(),
                userVoteResult.incorrectCount(),
                topVoters,
                mostIncorrectTmis
        );
    }
}
