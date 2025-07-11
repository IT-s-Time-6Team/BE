package com.team6.team6.balance.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;                        // 방 식별자
    private String voterName;                   // 투표자 닉네임
    private Long memberId;                      // 투표자 ID
    @Enumerated(EnumType.STRING)
    private BalanceChoice selectedChoice;       // A 또는 B
    private Integer votingRound;                // 투표 라운드 (문제 순서와 동일)
    private Long balanceSessionQuestionId;         // 해당 문제 ID

    @Builder
    private BalanceVote(Long roomId, String voterName, Long memberId,
                       BalanceChoice selectedChoice, Integer votingRound,
                       Long balanceSessionQuestionId) {
        this.roomId = roomId;
        this.voterName = voterName;
        this.memberId = memberId;
        this.selectedChoice = selectedChoice;
        this.votingRound = votingRound;
        this.balanceSessionQuestionId = balanceSessionQuestionId;
    }

    public static BalanceVote create(Long roomId, String voterName, Long memberId,
                                   BalanceChoice selectedChoice, int votingRound,
                                   Long balanceSessionQuestionId) {
        return BalanceVote.builder()
                .roomId(roomId)
                .voterName(voterName)
                .memberId(memberId)
                .selectedChoice(selectedChoice)
                .votingRound(votingRound)
                .balanceSessionQuestionId(balanceSessionQuestionId)
                .build();
    }
} 