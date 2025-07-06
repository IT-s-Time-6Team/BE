package com.team6.team6.balance.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalanceSessionQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;                // 방 식별자
    private Long balanceQuestionId;
    @Column(name = "question_a")// BALANCE_QUESTION 테이블 참조
    private String questionA;
    @Column(name = "question_b")// A 선택지 (복사해서 저장)
    private String questionB;           // B 선택지 (복사해서 저장)
    private Integer displayOrder;       // 문제 출제 순서 (0부터 시작)

    @Builder
    private BalanceSessionQuestion(Long roomId, Long balanceQuestionId, String questionA, 
                               String questionB, Integer displayOrder) {
        this.roomId = roomId;
        this.balanceQuestionId = balanceQuestionId;
        this.questionA = questionA;
        this.questionB = questionB;
        this.displayOrder = displayOrder;
    }

    public static BalanceSessionQuestion create(Long roomId, Long balanceQuestionId, 
                                           String questionA, String questionB, int displayOrder) {
        return BalanceSessionQuestion.builder()
                .roomId(roomId)
                .balanceQuestionId(balanceQuestionId)
                .questionA(questionA)
                .questionB(questionB)
                .displayOrder(displayOrder)
                .build();
    }
} 