package com.team6.team6.question.domain;

import com.team6.team6.question.entity.Question;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class QuestionsTest {

    @Test
    void getRandomSubset_요청_수보다_작거나_같으면_전체_반환() {
        // given
        List<Question> list = List.of(
                Question.of("test", "Q1", null),
                Question.of("test", "Q2", null)
        );
        Questions questions = Questions.of(list);

        // when
        List<Question> subset = questions.getRandomSubset(5);

        // then
        assertThat(subset).containsExactlyInAnyOrderElementsOf(list);
    }

    @Test
    void getRandomSubset_요청_수보다_많으면_랜덤_10개_반환() {
        // given
        List<Question> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            list.add(Question.of("test", "Q" + i, null));
        }
        Questions questions = Questions.of(list);

        // when
        List<Question> subset = questions.getRandomSubset(10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(subset).hasSize(10);
            softly.assertThat(list).containsAll(subset);
        });
    }
}

