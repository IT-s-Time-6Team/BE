package com.team6.team6.balance.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceMemberScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;            // 방 식별자
    private Long memberId;          // 참여자 ID
    private String memberName;      // 참여자 닉네임
    private Integer currentScore;   // 현재 점수
    private Integer totalCorrect;   // 총 맞춘 횟수 (다수파 선택)
    private Integer totalWrong;     // 총 틀린 횟수 (소수파 선택)

    @Builder
    private BalanceMemberScore(Long roomId, Long memberId, String memberName,
                              Integer currentScore, Integer totalCorrect, Integer totalWrong) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.currentScore = currentScore;
        this.totalCorrect = totalCorrect;
        this.totalWrong = totalWrong;
    }

    public static BalanceMemberScore createInitial(Long roomId, Long memberId, String memberName) {
        return BalanceMemberScore.builder()
                .roomId(roomId)
                .memberId(memberId)
                .memberName(memberName)
                .currentScore(0)
                .totalCorrect(0)
                .totalWrong(0)
                .build();
    }

    public void addScore(int points) {
        this.currentScore += points;
        if (points > 0) {
            this.totalCorrect++;
        } else if (points < 0) {
            this.totalWrong++;
        }
    }

    public void updateScore(boolean isMajority) {
        if (isMajority) {
            addScore(1);
        } else {
            addScore(-1);
        }
    }
} 