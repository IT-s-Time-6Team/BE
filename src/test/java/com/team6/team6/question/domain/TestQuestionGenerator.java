package com.team6.team6.question.domain;

import java.util.List;

public class TestQuestionGenerator implements QuestionGenerator {

    @Override
    public List<String> generateQuestions(String keyword) {
        try {
            Thread.sleep(2000); // 비동기 여부 확인용
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return List.of("Q1", "Q2", "Q3");
    }
}
