package com.team6.team6.question.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.keyword.entity.KeywordGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;
    private String question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private KeywordGroup keywordGroup;

    @Builder
    private Question(String keyword, String question, KeywordGroup keywordGroup) {
        this.question = question;
        this.keyword = keyword;
        this.keywordGroup = keywordGroup;
    }

    public static Question of(String keyword, String question, KeywordGroup keywordGroup) {
        return Question.builder()
                .keyword(keyword)
                .question(question)
                .keywordGroup(keywordGroup)
                .build();
    }
}
