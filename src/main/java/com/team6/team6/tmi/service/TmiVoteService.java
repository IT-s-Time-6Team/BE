package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.domain.TmiSubmissions;
import com.team6.team6.tmi.domain.TmiVotes;
import com.team6.team6.tmi.domain.VoteStatus;
import com.team6.team6.tmi.domain.repository.TmiSessionRepository;
import com.team6.team6.tmi.domain.repository.TmiSubmissionRepository;
import com.team6.team6.tmi.domain.repository.TmiVoteRepository;
import com.team6.team6.tmi.dto.TmiVoteServiceReq;
import com.team6.team6.tmi.dto.TmiVotingPersonalResult;
import com.team6.team6.tmi.dto.TmiVotingStartResponse;
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
@Transactional(readOnly = true)
public class TmiVoteService {

    private final TmiSessionService tmiSessionService;
    private final TmiSubmissionRepository tmiSubmissionRepository;
    private final TmiVoteRepository tmiVoteRepository;
    private final TmiMessagePublisher messagePublisher;

    @Transactional
    public void startVotingPhase(String roomKey, Long roomId) {
        TmiSession session = tmiSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증
        session.validateCanStartVoting();

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
        TmiSession session = tmiSessionService.findSessionByRoomId(req.roomId());

        // 상태 검증
        session.requireVotingPhase();
        validateDuplicateVote(req.roomId(), req.voterName(), session.getCurrentVotingTmiIndex());

        // 현재 투표 중인 TMI
        TmiSubmission currentTmi = getCurrentVotingTmiByDisplayOrder(req.roomId(), session.getCurrentVotingTmiIndex());

        // 투표 저장
        TmiVote vote = TmiVote.create(
                req.roomId(),
                req.voterName(),
                req.voterId(),
                req.voterCharacterType(),
                req.votedMemberName(),
                currentTmi.getMemberId(),
                currentTmi.getCharacterType(),
                currentTmi.getId(),
                session.getCurrentVotingTmiIndex()
        );
        vote.changeIsCorrect(currentTmi.getMemberName());
        tmiVoteRepository.save(vote);

        // 투표 처리 및 상태 전환 (TmiSession 내부에서 처리)
        VoteStatus result = session.processVote();

        log.debug("TMI 투표 제출: roomKey={}, voter={}, voted={}, round={}, result={}",
                req.roomKey(), req.voterName(), req.votedMemberName(), session.getCurrentVotingTmiIndex(), result);

        switch (result) {
            case IN_PROGRESS -> {
                // 현재 라운드 진행률 알림
                int progress = session.getCurrentRoundVotingProgress();
                messagePublisher.notifyTmiVotingProgress(req.roomKey(), progress);
            }
            case ROUND_COMPLETED -> {
                // 현재 라운드 완료, 다음 TMI로 이동
                messagePublisher.notifyTmiRoundCompleted(req.roomKey(), session.getCurrentVotingTmiIndex() - 1);
                messagePublisher.notifyTmiVotingStarted(req.roomKey());
            }
            case ALL_COMPLETED -> {
                // 모든 TMI 투표 완료
                messagePublisher.notifyTmiAllVotingCompleted(req.roomKey());
            }
        }
    }

    public TmiVotingStartResponse getCurrentVotingInfo(Long roomId) {
        TmiSession session = tmiSessionService.findSessionByRoomId((roomId));

        // 상태 검증
        session.requireVotingPhase();

        TmiSubmission currentTmi = getCurrentVotingTmiByDisplayOrder(roomId, session.getCurrentVotingTmiIndex());
        List<String> memberNames = getMemberNamesFromRoom(roomId);

        return TmiVotingStartResponse.of(
                currentTmi.getTmiContent(),
                currentTmi.getDisplayOrder(),
                memberNames
        );
    }

    public TmiVotingPersonalResult getLatestVotingResult(Long roomId, String memberName) {
        TmiSession session = tmiSessionService.findSessionByRoomId(roomId);

        // 가장 마지막에 끝난 투표 라운드 찾기
        int latestCompletedRound = session.getLatestCompletedRound();

        if (latestCompletedRound < 0) {
            throw new IllegalStateException("완료된 투표가 없습니다");
        }

        // 해당 라운드의 TMI 조회
        TmiSubmission tmi = getCurrentVotingTmiByDisplayOrder(roomId, latestCompletedRound);

        // 해당 라운드의 모든 투표 조회
        List<TmiVote> voteList = tmiVoteRepository.findByRoomIdAndVotingRound(roomId, latestCompletedRound);
        TmiVotes votes = TmiVotes.from(voteList);
        TmiVote myVote = votes.findVoteByName(memberName);

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
