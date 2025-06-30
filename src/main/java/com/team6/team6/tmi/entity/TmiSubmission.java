package com.team6.team6.tmi.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmiSubmission extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private Long memberId;
    private String memberName;
    private String tmiContent;
    @Setter
    private Integer displayOrder;  // 랜덤 순서로 배정

    @Builder
    private TmiSubmission(Long roomId, Long memberId, String memberName, String tmiContent, Integer displayOrder) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.tmiContent = tmiContent;
        this.displayOrder = displayOrder;
    }

    public static TmiSubmission create(Long roomId, Long memberId, String memberName, String tmiContent) {
        return TmiSubmission.builder()
                .roomId(roomId)
                .memberId(memberId)
                .memberName(memberName)
                .tmiContent(tmiContent)
                .displayOrder(null)
                .build();
    }
}
