package com.team6.team6.question.dto;

import com.team6.team6.question.entity.Question;

public record QuestionResponse(Long id, String keyword, String question) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getKeyword(), question.getQuestion());
    }
}
