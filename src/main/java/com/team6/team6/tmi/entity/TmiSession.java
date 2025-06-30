package com.team6.team6.tmi.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.tmi.domain.VoteStatus;
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
public class TmiSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    @Enumerated(EnumType.STRING)
    private TmiGameStep currentStep;
    private Integer totalMembers;           // 총 멤버 수
    private Integer submittedTmiCount;      // 제출된 TMI 수
    private Integer currentVotingTmiIndex;     // 현재 투표 중인 TMI 인덱스
    private Integer currentVotedMemberCount;       // 현재 라운드 투표 완료 멤버 수
    private LocalDateTime closedAt;

    @Builder
    private TmiSession(Long roomId, TmiGameStep currentStep, Integer totalMembers,
                       Integer submittedTmiCount, Integer currentVotingTmiIndex,
                       Integer currentVotedMemberCount) {
        this.roomId = roomId;
        this.currentStep = currentStep;
        this.totalMembers = totalMembers;
        this.submittedTmiCount = submittedTmiCount;
        this.currentVotingTmiIndex = currentVotingTmiIndex;
        this.currentVotedMemberCount = currentVotedMemberCount;
    }

    public static TmiSession createInitialSession(Long roomId, int totalMembers) {
        return TmiSession.builder()
                .roomId(roomId)
                .totalMembers(totalMembers)
                .currentStep(TmiGameStep.COLLECTING_TMI)
                .submittedTmiCount(0)
                .currentVotedMemberCount(0)
                .currentVotingTmiIndex(0)
                .build();
    }

    // ==================== 상태 검증 메서드 ====================

    public void requireCollectingTmiPhase() {
        if (this.currentStep != TmiGameStep.COLLECTING_TMI) {
            throw new IllegalStateException("TMI 수집 단계가 아닙니다");
        }
    }

    public void requireHintPhase() {
        if (this.currentStep != TmiGameStep.HINT) {
            throw new IllegalStateException("힌트 단계가 아닙니다");
        }
    }

    public void requireVotingPhase() {
        if (this.currentStep != TmiGameStep.VOTING) {
            throw new IllegalStateException("투표 단계가 아닙니다");
        }
    }

    public void requireCompletedPhase() {
        if (this.currentStep != TmiGameStep.COMPLETED) {
            throw new IllegalStateException("게임이 완료되지 않았습니다");
        }
    }

    public void validateCanStartHint() {
        if (this.currentStep != TmiGameStep.COLLECTING_TMI) {
            throw new IllegalStateException("TMI 수집 단계가 아닙니다");
        }
        if (!isAllTmiCollected()) {
            throw new IllegalStateException("모든 TMI가 수집되지 않았습니다");
        }
    }

    public void validateCanStartVoting() {
        if (this.currentStep != TmiGameStep.HINT) {
            throw new IllegalStateException("투표를 시작할 수 없는 상태입니다");
        }
        if (!isAllTmiCollected()) {
            throw new IllegalStateException("모든 TMI가 수집되지 않았습니다");
        }
    }

    // ==================== 상태 전환 메서드 ====================

    public void incrementSubmittedTmiCount() {
        requireCollectingTmiPhase();
        this.submittedTmiCount++;
    }

    public void startHintTime() {
        validateCanStartHint();
        this.currentStep = TmiGameStep.HINT;
    }

    public void startVotingPhase() {
        validateCanStartVoting();
        this.currentStep = TmiGameStep.VOTING;
        this.currentVotingTmiIndex = 0;
        this.currentVotedMemberCount = 0;
    }

    /**
     * 투표 수를 증가시키고 필요할 경우 다음 TMI로 넘어가거나 게임을 완료합니다.
     *
     * @return VoteResult 투표 후 결과 상태
     */
    public VoteStatus processVote() {
        requireVotingPhase();
        this.currentVotedMemberCount++;

        // 현재 라운드 투표가 완료되지 않았으면 진행 중 상태 반환
        if (!isCurrentRoundVotingCompleted()) {
            return VoteStatus.IN_PROGRESS;
        }

        // 마지막 TMI에 대한 투표였다면 게임 완료
        if (isLastTmiIndex()) {
            this.currentStep = TmiGameStep.COMPLETED;
            this.closedAt = LocalDateTime.now();
            return VoteStatus.ALL_COMPLETED;
        }

        // 다음 TMI로 이동
        this.currentVotingTmiIndex++;
        this.currentVotedMemberCount = 0;
        return VoteStatus.ROUND_COMPLETED;
    }

    // ==================== 상태 확인 메서드 ====================

    public boolean isCollectingPhase() {
        return this.currentStep == TmiGameStep.COLLECTING_TMI;
    }

    public boolean isHintPhase() {
        return this.currentStep == TmiGameStep.HINT;
    }

    public boolean isVotingPhase() {
        return this.currentStep == TmiGameStep.VOTING;
    }

    public boolean isCompletedPhase() {
        return this.currentStep == TmiGameStep.COMPLETED;
    }

    // TMI 수집 진행률 계산 (0~100%)
    public int calculateCollectionProgress() {
        if (totalMembers == 0) {
            return 0;
        }
        return (submittedTmiCount * 100) / totalMembers;
    }

    // 모든 TMI 수집 완료 여부
    public boolean isAllTmiCollected() {
        return submittedTmiCount.equals(totalMembers);
    }

    public boolean isCurrentRoundVotingCompleted() {
        return currentVotedMemberCount >= totalMembers;
    }

    public boolean isLastTmiIndex() {
        return currentVotingTmiIndex == (totalMembers - 1);
    }

    public int getCurrentRoundVotingProgress() {
        if (totalMembers == 0) return 0;
        return (currentVotedMemberCount * 100) / totalMembers;
    }

    public int getLatestCompletedRound() {
        if (isCompletedPhase()) {
            return currentVotingTmiIndex;
        } else if (isVotingPhase() && currentVotingTmiIndex > 0) {
            return currentVotingTmiIndex - 1;
        }
        return -1; // 완료된 투표가 없음
    }

    public void validateCompleted() {
        if (this.currentStep != TmiGameStep.COMPLETED) {
            throw new IllegalStateException("TMI 게임이 아직 완료되지 않았습니다. 현재 단계: " + this.currentStep);
        }
    }
}
