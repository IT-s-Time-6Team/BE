package com.team6.team6.balance.entity;

import com.team6.team6.balance.domain.VoteStatus;
import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    @Enumerated(EnumType.STRING)
    private BalanceGameStep currentStep;
    private Integer totalMembers;               // 총 참여자 수
    private Integer totalQuestions;             // 총 문제 수
    private Integer currentQuestionIndex;       // 현재 문제 인덱스 (0부터 시작)
    private Integer currentVotedMemberCount;    // 현재 라운드 투표 완료자 수
    private Integer currentResultViewedCount;   // 현재 라운드 결과 확인 완료자 수
    private LocalDateTime closedAt;             // 게임 종료 시간

    @Builder
    private BalanceSession(Long roomId, BalanceGameStep currentStep, Integer totalMembers,
                          Integer totalQuestions, Integer currentQuestionIndex,
                          Integer currentVotedMemberCount, Integer currentResultViewedCount) {
        this.roomId = roomId;
        this.currentStep = currentStep;
        this.totalMembers = totalMembers;
        this.totalQuestions = totalQuestions;
        this.currentQuestionIndex = currentQuestionIndex;
        this.currentVotedMemberCount = currentVotedMemberCount;
        this.currentResultViewedCount = currentResultViewedCount;
    }

    public static BalanceSession createInitialSession(Long roomId, int totalMembers, int totalQuestions) {
        return BalanceSession.builder()
                .roomId(roomId)
                .totalMembers(totalMembers)
                .totalQuestions(totalQuestions)
                .currentStep(BalanceGameStep.WAITING_FOR_MEMBERS)
                .currentQuestionIndex(0)
                .currentVotedMemberCount(0)
                .currentResultViewedCount(0)
                .build();
    }

    // ==================== 상태 검증 메서드 ====================

    public void requireWaitingForMembersPhase() {
        if (this.currentStep != BalanceGameStep.WAITING_FOR_MEMBERS) {
            throw new IllegalStateException("멤버 대기 단계가 아닙니다");
        }
    }

    public void requireQuestionRevealPhase() {
        if (this.currentStep != BalanceGameStep.QUESTION_REVEAL) {
            throw new IllegalStateException("문제 공개 단계가 아닙니다");
        }
    }

    public void requireDiscussionPhase() {
        if (this.currentStep != BalanceGameStep.DISCUSSION) {
            throw new IllegalStateException("토론 단계가 아닙니다");
        }
    }

    public void requireVotingPhase() {
        if (this.currentStep != BalanceGameStep.VOTING) {
            throw new IllegalStateException("투표 단계가 아닙니다");
        }
    }

    public void requireResultViewPhase() {
        if (this.currentStep != BalanceGameStep.RESULT_VIEW) {
            throw new IllegalStateException("결과 확인 단계가 아닙니다");
        }
    }

    public void requireCompletedPhase() {
        if (this.currentStep != BalanceGameStep.COMPLETED) {
            throw new IllegalStateException("게임이 완료되지 않았습니다");
        }
    }

    public void validateCanStartQuestionReveal() {
        if (this.currentStep != BalanceGameStep.WAITING_FOR_MEMBERS) {
            throw new IllegalStateException("멤버 대기 단계가 아닙니다");
        }
    }

    public void validateCanStartDiscussion() {
        if (this.currentStep != BalanceGameStep.QUESTION_REVEAL) {
            throw new IllegalStateException("문제 공개 단계가 아닙니다");
        }
    }

    public void validateCanStartVoting() {
        if (this.currentStep != BalanceGameStep.DISCUSSION) {
            throw new IllegalStateException("투표를 시작할 수 없는 상태입니다");
        }
    }

    public void validateCanStartResultView() {
        if (this.currentStep != BalanceGameStep.VOTING) {
            throw new IllegalStateException("결과 확인을 시작할 수 없는 상태입니다");
        }
        if (!isCurrentRoundVotingCompleted()) {
            throw new IllegalStateException("모든 투표가 완료되지 않았습니다");
        }
    }

    // ==================== 상태 전환 메서드 ====================

    public void startQuestionRevealPhase() {
        validateCanStartQuestionReveal();
        this.currentStep = BalanceGameStep.QUESTION_REVEAL;
    }

    public void startDiscussionPhase() {
        validateCanStartDiscussion();
        this.currentStep = BalanceGameStep.DISCUSSION;
    }

    public void startVotingPhase() {
        validateCanStartVoting();
        this.currentStep = BalanceGameStep.VOTING;
        this.currentVotedMemberCount = 0;
    }

    public void startResultViewPhase() {
        validateCanStartResultView();
        this.currentStep = BalanceGameStep.RESULT_VIEW;
        this.currentResultViewedCount = 0; // 결과 확인 수 초기화
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < totalQuestions;
    }

    public void moveToNextQuestion() {
        if (!hasMoreQuestions()) {
            throw new IllegalStateException("더 이상 문제가 없습니다");
        }
        this.currentQuestionIndex++;
        this.currentVotedMemberCount = 0; // 투표 수 초기화
        this.currentResultViewedCount = 0; // 결과 확인 수 초기화
        this.currentStep = BalanceGameStep.WAITING_FOR_MEMBERS; // 다음 라운드를 위해 멤버 대기 상태로 변경
    }

    public void completeGame() {
        this.currentStep = BalanceGameStep.COMPLETED;
        this.closedAt = LocalDateTime.now();
    }

    public void requireGameCompleted() {
        if (this.currentStep != BalanceGameStep.COMPLETED) {
            throw new IllegalStateException("게임이 완료되지 않았습니다");
        }
    }

    /**
     * 투표 수를 증가시키고 필요할 경우 다음 문제로 넘어가거나 게임을 완료합니다.
     *
     * @return VoteStatus 투표 후 결과 상태
     */
    public VoteStatus processVote() {
        requireVotingPhase();
        this.currentVotedMemberCount++;

        // 현재 라운드 투표가 완료되지 않았으면 진행 중 상태 반환
        if (!isCurrentRoundVotingCompleted()) {
            return VoteStatus.IN_PROGRESS;
        }

        // 마지막 문제에 대한 투표였다면 게임 완료 준비
        if (isLastQuestionIndex()) {
            return VoteStatus.ALL_COMPLETED;
        }

        // 다음 문제로 이동 준비
        return VoteStatus.ROUND_COMPLETED;
    }

    /**
     * 결과 확인 완료 처리
     * @return 모든 멤버가 결과 확인을 완료했는지 여부
     */
    public boolean processResultViewReady() {
        requireResultViewPhase();
        this.currentResultViewedCount++;
        
        return isAllResultViewCompleted();
    }

    /**
     * 모든 멤버가 결과 확인을 완료했는지 확인
     */
    public boolean isAllResultViewCompleted() {
        return currentResultViewedCount >= totalMembers;
    }

    /**
     * 결과 확인 진행률 계산 (0-100)
     */
    public int getResultViewProgress() {
        if (totalMembers == 0) return 100;
        return (currentResultViewedCount * 100) / totalMembers;
    }

    // ==================== 상태 확인 메서드 ====================

    public boolean isWaitingForMembersPhase() {
        return this.currentStep == BalanceGameStep.WAITING_FOR_MEMBERS;
    }

    public boolean isQuestionRevealPhase() {
        return this.currentStep == BalanceGameStep.QUESTION_REVEAL;
    }

    public boolean isDiscussionPhase() {
        return this.currentStep == BalanceGameStep.DISCUSSION;
    }

    public boolean isVotingPhase() {
        return this.currentStep == BalanceGameStep.VOTING;
    }

    public boolean isResultViewPhase() {
        return this.currentStep == BalanceGameStep.RESULT_VIEW;
    }

    public boolean isCompletedPhase() {
        return this.currentStep == BalanceGameStep.COMPLETED;
    }

    public boolean isCurrentRoundVotingCompleted() {
        return currentVotedMemberCount >= totalMembers;
    }

    public boolean isLastQuestionIndex() {
        return currentQuestionIndex == (totalQuestions - 1);
    }

    public int getCurrentRoundVotingProgress() {
        if (totalMembers == 0) return 0;
        return (currentVotedMemberCount * 100) / totalMembers;
    }

    // ==================== 멤버 입장 관련 메서드 ====================

    /**
     * 현재 입장한 멤버 수와 총 멤버 수를 비교하여 모든 멤버가 입장했는지 확인
     * @param currentMemberCount 현재 방에 입장한 멤버 수
     * @return 모든 멤버가 입장했으면 true
     */
    public boolean isAllMembersJoined(int currentMemberCount) {
        return currentMemberCount >= totalMembers;
    }

    /**
     * 멤버 입장 진행률을 계산 (0-100)
     * @param currentMemberCount 현재 방에 입장한 멤버 수
     * @return 입장 진행률 (백분율)
     */
    public int getMemberJoinProgress(int currentMemberCount) {
        if (totalMembers == 0) return 100;
        return Math.min((currentMemberCount * 100) / totalMembers, 100);
    }
} 