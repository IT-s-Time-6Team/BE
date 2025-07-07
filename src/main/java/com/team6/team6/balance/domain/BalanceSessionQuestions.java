package com.team6.team6.balance.domain;

import com.team6.team6.balance.entity.BalanceSessionQuestion;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BalanceSessionQuestions {
    private final List<BalanceSessionQuestion> questions;

    private BalanceSessionQuestions(List<BalanceSessionQuestion> questions) {
        this.questions = List.copyOf(questions);
    }

    public static BalanceSessionQuestions from(List<BalanceSessionQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("밸런스 게임 문제 목록이 비어있습니다");
        }
        return new BalanceSessionQuestions(questions);
    }

    public BalanceSessionQuestion getQuestionByOrder(int order) {
        return questions.stream()
                .filter(question -> question.getDisplayOrder().equals(order))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 순서의 문제를 찾을 수 없습니다: " + order));
    }

    public int getTotalCount() {
        return questions.size();
    }

    public int getRemainingCount(int currentIndex) {
        return getTotalCount() - currentIndex - 1;
    }

    public boolean isEmpty() {
        return questions.isEmpty();
    }

    public boolean isValidOrder(int order) {
        return order >= 0 && order < getTotalCount();
    }
} 