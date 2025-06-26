package com.team6.team6.tmi.entity;

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
public class TmiSubmission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long memberId;
    private String tmiContent;
    private Integer displayOrder;  // 랜덤 순서로 배정

    @Builder
    private TmiSubmission(Long roomId, Long memberId, String tmiContent, Integer displayOrder) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.tmiContent = tmiContent;
        this.displayOrder = displayOrder;
    }

    public static TmiSubmission create(Long roomId, Long memberId, String tmiContent) {
        return TmiSubmission.builder()
                .roomId(roomId)
                .memberId(memberId)
                .tmiContent(tmiContent)
                .displayOrder(null)
                .build();
    }
}
