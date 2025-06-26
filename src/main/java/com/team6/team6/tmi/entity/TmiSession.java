package com.team6.team6.tmi.entity;

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

    public void incrementSubmittedTmiCount() {
        this.submittedTmiCount++;
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
}
