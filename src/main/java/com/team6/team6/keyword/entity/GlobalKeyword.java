package com.team6.team6.keyword.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GlobalKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private KeywordGroup keywordGroup;

    @Builder
    private GlobalKeyword(String keyword, KeywordGroup keywordGroup) {
        this.keyword = keyword;
        this.keywordGroup = keywordGroup;
    }

    public static GlobalKeyword create(String keyword, KeywordGroup keywordGroup) {
        return GlobalKeyword.builder()
                .keyword(keyword)
                .keywordGroup(keywordGroup)
                .build();
    }
}
