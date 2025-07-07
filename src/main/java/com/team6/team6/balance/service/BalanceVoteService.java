package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.domain.BalanceVoteResult;
import com.team6.team6.balance.domain.BalanceVotes;
import com.team6.team6.balance.domain.VoteStatus;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceVoteServiceReq;
import com.team6.team6.balance.dto.BalanceVotingStartResponse;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.balance.entity.BalanceVote;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BalanceVoteService {

    private final BalanceSessionService balanceSessionService;
    private final BalanceQuestionService balanceQuestionService;
    private final BalanceVoteRepository balanceVoteRepository;
    private final BalanceMessagePublisher messagePublisher;
    private final BalanceScoreService balanceScoreService;

    @Transactional
    public void startVotingPhase(String roomKey, Long roomId) {
        BalanceSession session = balanceSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증
        session.validateCanStartVoting();

        // 세션을 투표 단계로 변경
        session.startVotingPhase();

        log.debug("밸런스 투표 단계 시작: roomKey={}, currentRound={}", roomKey, session.getCurrentQuestionIndex());

        // 투표 시작 알림
        messagePublisher.notifyBalanceVotingStarted(roomKey);
    }



    @Transactional
    public void submitVote(BalanceVoteServiceReq req) {
        BalanceSession session = balanceSessionService.findSessionByRoomIdWithLock(req.roomId());

        // 상태 검증
        session.requireVotingPhase();
        validateDuplicateVote(req.roomId(), req.memberId(), session.getCurrentQuestionIndex());

        // 현재 투표 중인 문제
        BalanceSessionQuestion currentQuestion = balanceQuestionService.getCurrentQuestionByDisplayOrder(
                req.roomId(), session.getCurrentQuestionIndex());

        // 투표 저장
        BalanceVote vote = BalanceVote.create(
                req.roomId(),
                req.memberName(),
                req.memberId(),
                req.selectedChoice(),
                session.getCurrentQuestionIndex(),
                currentQuestion.getId()
        );
        balanceVoteRepository.save(vote);

        // 투표 처리 및 상태 전환
        VoteStatus result = session.processVote();

        log.debug("밸런스 투표 제출: roomKey={}, voter={}, choice={}, round={}, result={}",
                req.roomKey(), req.memberName(), req.selectedChoice(), 
                session.getCurrentQuestionIndex(), result);

        switch (result) {
            case IN_PROGRESS -> {
                // 현재 라운드 진행률 알림
                int progress = session.getCurrentRoundVotingProgress();
                messagePublisher.notifyBalanceVotingProgress(req.roomKey(), progress);
            }
            case ROUND_COMPLETED -> {
                // 점수 계산 및 업데이트
                balanceScoreService.calculateAndUpdateScores(req.roomId(), session.getCurrentQuestionIndex());
                
                // 현재 라운드 완료, 결과 확인 단계로 이동
                session.startResultViewPhase();
                messagePublisher.notifyBalanceRoundCompleted(req.roomKey(), session.getCurrentQuestionIndex());
            }
            case ALL_COMPLETED -> {
                // 점수 계산 및 업데이트
                balanceScoreService.calculateAndUpdateScores(req.roomId(), session.getCurrentQuestionIndex());
                
                // 모든 투표 완료, 바로 게임 종료
                session.completeGame();
                
                log.debug("밸런스 게임 완료: roomKey={}", req.roomKey());
                messagePublisher.notifyBalanceGameCompleted(req.roomKey());
            }
        }
    }

    private void validateDuplicateVote(Long roomId, Long memberId, int currentRound) {
        if (balanceVoteRepository.existsByRoomIdAndMemberIdAndVotingRound(roomId, memberId, currentRound)) {
            throw new IllegalStateException("이미 이 라운드에 투표했습니다");
        }
    }

    public BalanceVoteResult getRoundVoteResult(Long roomId, int round) {
        List<BalanceVote> votes = balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round);
        BalanceVotes balanceVotes = BalanceVotes.from(votes);
        return balanceVotes.calculateVoteResult();
    }

    public BalanceVotingStartResponse getCurrentVotingInfo(Long roomId) {
        BalanceSession session = balanceSessionService.findSessionByRoomId(roomId);

        // 상태 검증 없음 - 언제든 현재 문제 조회 가능

        // 현재 문제 조회
        BalanceSessionQuestion currentQuestion = balanceQuestionService.getCurrentQuestionByDisplayOrder(
                roomId, session.getCurrentQuestionIndex());

        return BalanceVotingStartResponse.of(
                currentQuestion.getQuestionA(),
                currentQuestion.getQuestionB()
        );
    }
} 