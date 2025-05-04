package com.team6.team6.question.entity;

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
public class Question extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String keyword;
    private String question;

    @Builder
    private Question(String keyword, String question) {
        this.question = question;
        this.keyword = keyword;
    }

    public static Question of(String keyword, String question) {
        return Question.builder()
                .keyword(keyword)
                .question(question)
                .build();
    }
}
