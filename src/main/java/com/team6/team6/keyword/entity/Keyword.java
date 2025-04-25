package com.team6.team6.keyword.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;
    private Long roomId;
    private Long memberId;

    @Builder
    private Keyword(String keyword, Long roomId, Long memberId) {
        this.keyword = keyword;
        this.roomId = roomId;
        this.memberId = memberId;
    }

    public static Keyword of(String keyword, Long roomId, Long memberId) {
        return Keyword.builder()
                .keyword(keyword)
                .roomId(roomId)
                .memberId(memberId)
                .build();
    }


}
