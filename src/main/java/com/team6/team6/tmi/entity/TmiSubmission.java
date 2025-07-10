package com.team6.team6.tmi.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.member.entity.CharacterType;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type")
    private CharacterType characterType;
    private String tmiContent;
    @Setter
    private Integer displayOrder;  // 랜덤 순서로 배정

    @Builder
    private TmiSubmission(Long roomId, Long memberId, String memberName, String tmiContent, Integer displayOrder, CharacterType characterType) {
        this.roomId = roomId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.tmiContent = tmiContent;
        this.displayOrder = displayOrder;
        this.characterType = characterType;
    }

    public static TmiSubmission create(Long roomId, Long memberId, String memberName, String tmiContent, CharacterType characterType) {
        return TmiSubmission.builder()
                .roomId(roomId)
                .memberId(memberId)
                .memberName(memberName)
                .tmiContent(tmiContent)
                .characterType(characterType)
                .displayOrder(null)
                .build();
    }
}
