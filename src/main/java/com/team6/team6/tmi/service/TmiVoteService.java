package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.domain.TmiSubmissions;
import com.team6.team6.tmi.domain.TmiVotes;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmiVoteService {

    private final TmiSessionRepository tmiSessionRepository;
    private final TmiSubmissionRepository tmiSubmissionRepository;
    private final TmiVoteRepository tmiVoteRepository;
    private final TmiMessagePublisher messagePublisher;

    @Transactional
    public void startVotingPhase(String roomKey, Long roomId) {
        TmiSession session = findTmiSession(roomId);

        validateCanStartVoting(session);

        // TMI 목록을 일급 컬렉션으로 관리하고 랜덤 배치
        List<TmiSubmission> submissions = tmiSubmissionRepository.findByRoomId(roomId);
        TmiSubmissions submissionList = TmiSubmissions.from(submissions);
        TmiSubmissions shuffledList = submissionList.shuffleForVoting();

        // 세션을 투표 단계로 변경
        session.startVotingPhase();

        log.debug("TMI 투표 단계 시작: roomKey={}, totalTmi={}", roomKey, shuffledList.getTotalCount());

        // 첫 번째 TMI 투표 시작 알림
        messagePublisher.notifyTmiVotingStarted(roomKey);
    }

    @Transactional
    public void submitVote(TmiVoteServiceReq req) {
        TmiSession session = findTmiSession(req.roomId());

        validateVotingPhase(session);
        validateDuplicateVote(req.roomId(), req.voterName(), session.getCurrentVotingTmiIndex());

        // 현재 투표 중인 TMI
        TmiSubmission currentTmi = getCurrentVotingTmiByDisplayOrder(req.roomId(), session.getCurrentVotingTmiIndex());

        // 투표 저장
        TmiVote vote = TmiVote.create(
                req.roomId(),
                req.voterName(),
                req.votedMemberName(),
                currentTmi.getId(),
                session.getCurrentVotingTmiIndex()
        );
        vote.changeIsCorrect(currentTmi.getMemberName());
        tmiVoteRepository.save(vote);

        // 세션 투표 카운트 증가
        session.incrementVotedMemberCount();

        boolean isRoundCompleted = session.isCurrentRoundVotingCompleted();

        log.debug("TMI 투표 제출: roomKey={}, voter={}, voted={}, round={}, isRoundCompleted={}",
                req.roomKey(), req.voterName(), req.votedMemberName(), session.getCurrentVotingTmiIndex(), isRoundCompleted);

        if (isRoundCompleted) {
            if (session.isLastTmiIndex()) {
                // 모든 TMI 투표 완료
                session.completeVoting();
                messagePublisher.notifyTmiAllVotingCompleted(req.roomKey());
            } else {
                // 다음 TMI 투표 시작
                messagePublisher.notifyTmiRoundCompleted(req.roomKey(), session.getCurrentVotingTmiIndex());
                session.moveToNextTmi();
                messagePublisher.notifyTmiVotingStarted(req.roomKey());
            }
        } else {
            // 현재 라운드 진행률 알림
            int progress = session.getCurrentRoundVotingProgress();
            messagePublisher.notifyTmiVotingProgress(req.roomKey(), progress);
        }
    }

    @Transactional(readOnly = true)
    public TmiVotingStartResponse getCurrentVotingInfo(Long roomId) {
        TmiSession session = findTmiSession(roomId);

        validateVotingPhase(session);

        TmiSubmission currentTmi = getCurrentVotingTmiByDisplayOrder(roomId, session.getCurrentVotingTmiIndex());
        List<String> memberNames = getMemberNamesFromRoom(roomId);

        return TmiVotingStartResponse.of(
                currentTmi.getTmiContent(),
                currentTmi.getDisplayOrder(),
                memberNames
        );
    }

    @Transactional(readOnly = true)
    public TmiVotingPersonalResult getLatestVotingResult(Long roomId, String memberName) {
        TmiSession session = findTmiSession(roomId);

        // 가장 마지막에 끝난 투표 라운드 찾기
        int latestCompletedRound = getLatestCompletedRound(session);

        if (latestCompletedRound < 0) {
            throw new IllegalStateException("완료된 투표가 없습니다");
        }

        // 해당 라운드의 TMI 조회
        TmiSubmission tmi = getCurrentVotingTmiByDisplayOrder(roomId, latestCompletedRound);

        // 해당 라운드의 모든 투표 조회
        List<TmiVote> voteList = tmiVoteRepository.findByRoomIdAndVotingRound(roomId, latestCompletedRound);
        TmiVotes votes = TmiVotes.from(voteList);

        TmiVote myVote = votes.getMyVote(memberName);

        return TmiVotingPersonalResult.of(
                tmi.getTmiContent(),
                tmi.getMemberName(),
                myVote.getVotedMemberName(),
                myVote.getIsCorrect(),
                votes.getVotingResults(),
                latestCompletedRound
        );
    }

    private List<String> getMemberNamesFromRoom(Long roomId) {
        return tmiSubmissionRepository.findByRoomId(roomId).stream()
                .map(TmiSubmission::getMemberName)
                .toList();
    }


    private int getLatestCompletedRound(TmiSession session) {
        if (session.getCurrentStep() == TmiGameStep.COMPLETED) {
            // 모든 투표가 완료된 경우 마지막 라운드
            return session.getCurrentVotingTmiIndex();
        } else if (session.getCurrentStep() == TmiGameStep.VOTING && session.getCurrentVotingTmiIndex() > 0) {
            // 투표 중이지만 이전 라운드가 있는 경우
            return session.getCurrentVotingTmiIndex() - 1;
        }
        return -1; // 완료된 투표가 없음
    }

    private TmiSession findTmiSession(Long roomId) {
        return tmiSessionRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> new IllegalStateException("TMI 게임 세션을 찾을 수 없습니다: " + roomId));
    }

    private void validateCanStartVoting(TmiSession session) {
        if (session.getCurrentStep() != TmiGameStep.COLLECTING_TMI) {
            throw new IllegalStateException("TMI 수집이 완료되지 않았습니다");
        }
        if (!session.isAllTmiCollected()) {
            throw new IllegalStateException("모든 TMI가 수집되지 않았습니다");
        }
    }

    private void validateVotingPhase(TmiSession session) {
        if (session.getCurrentStep() != TmiGameStep.VOTING) {
            throw new IllegalStateException("투표 단계가 아닙니다");
        }
    }

    private void validateDuplicateVote(Long roomId, String voterName, int votingRound) {
        if (tmiVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(roomId, voterName, votingRound)) {
            throw new IllegalStateException("이미 이 라운드에 투표했습니다");
        }
    }

    private TmiSubmission getCurrentVotingTmiByDisplayOrder(Long roomId, int displayOrder) {
        return tmiSubmissionRepository.findByRoomIdAndDisplayOrder(roomId, displayOrder)
                .orElseThrow(() -> new IllegalStateException("투표할 TMI를 찾을 수 없습니다: displayOrder=" + displayOrder));
    }
}
