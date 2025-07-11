package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.repository.BalanceSessionRepository;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceSessionStatusResponse;
import com.team6.team6.balance.entity.BalanceGameStep;
import com.team6.team6.balance.entity.BalanceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceSessionService {

    private final BalanceSessionRepository balanceSessionRepository;
    private final BalanceVoteRepository balanceVoteRepository;

    @Transactional
    public void createBalanceGameSession(Long roomId, int totalMembers, int totalQuestions) {
        BalanceSession session = BalanceSession.createInitialSession(roomId, totalMembers, totalQuestions);
        balanceSessionRepository.save(session);
        log.info("밸런스 게임 세션 생성: roomId={}, totalMembers={}, totalQuestions={}", 
                roomId, totalMembers, totalQuestions);
    }

    public BalanceSessionStatusResponse getSessionStatus(Long roomId, String memberName) {
        BalanceSession session = findSessionByRoomId(roomId);
        BalanceGameStep currentStep = session.getCurrentStep();
        boolean hasUserSubmitted = false;
        boolean waitingForOthers = false;
        int progress = 100;

        // 현재 단계에 따른 처리
        switch (currentStep) {
            case WAITING_FOR_MEMBERS -> {
                // 멤버 대기 단계: 입장만 하면 됨
                hasUserSubmitted = true;
                // 진행률은 Room에서 현재 멤버 수를 가져와서 계산해야 함 (여기서는 임시로 100)
                progress = 100;
            }
            case QUESTION_REVEAL -> {
                // 문제 공개 단계: 항상 false (참여만 하면 됨)
                hasUserSubmitted = true;
            }
            case DISCUSSION -> {
                // 토론 단계: 항상 true (참여만 하면 됨)
                hasUserSubmitted = true;
            }
            case VOTING -> {
                // 투표 단계: 현재 라운드에 유저가 투표했는지 확인
                Integer currentQuestionIndex = session.getCurrentQuestionIndex();
                hasUserSubmitted = balanceVoteRepository.existsByRoomIdAndVoterNameAndVotingRound(
                        roomId, memberName, currentQuestionIndex);
                progress = session.getCurrentRoundVotingProgress();
            }
            case RESULT_VIEW -> {
                // 결과 확인 단계
                hasUserSubmitted = true;
                waitingForOthers = !session.isAllResultViewCompleted();
                progress = session.getResultViewProgress();
            }
            case COMPLETED -> {
                // 게임 완료 단계: 항상 true
                hasUserSubmitted = true;
            }
        }

        return BalanceSessionStatusResponse.of(
                currentStep,
                hasUserSubmitted,
                waitingForOthers,
                progress,
                session.getCurrentQuestionIndex(),
                session.getTotalQuestions()
        );
    }


    public BalanceSession findSessionByRoomIdWithLock(Long roomId) {
        return balanceSessionRepository.findByRoomIdWithLock(roomId)
                .orElseThrow(() -> new IllegalStateException("밸런스 게임 세션을 찾을 수 없습니다: " + roomId));
    }

    public BalanceSession findSessionByRoomId(Long roomId) {
        return balanceSessionRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalStateException("밸런스 게임 세션을 찾을 수 없습니다: " + roomId));
    }

    /**
     * 멤버 입장 시 체크하고 모든 멤버가 입장하면 문제 공개 단계 시작
     * @param roomId 방 ID
     * @param currentMemberCount 현재 입장한 멤버 수
     * @return true면 문제 공개 단계 시작됨, false면 아직 대기 중
     */
    @Transactional
    public boolean checkMemberJoinAndStartIfReady(Long roomId, int currentMemberCount) {
        BalanceSession session = findSessionByRoomIdWithLock(roomId);
        
        // WAITING_FOR_MEMBERS 상태가 아니면 이미 시작된 것
        if (!session.isWaitingForMembersPhase()) {
            return false;
        }
        
        // 모든 멤버가 입장했는지 체크
        if (session.isAllMembersJoined(currentMemberCount)) {
            // 문제 공개 단계로 전환
            session.startQuestionRevealPhase();
            log.info("모든 멤버 입장 완료, 문제 공개 단계 시작: roomId={}, memberCount={}", 
                    roomId, currentMemberCount);
            return true;
        }
        
        return false;
    }
} 