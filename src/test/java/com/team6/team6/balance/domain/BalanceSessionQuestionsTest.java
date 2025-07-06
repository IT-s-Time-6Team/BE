package com.team6.team6.balance.domain;

import com.team6.team6.balance.entity.BalanceSessionQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("BalanceSessionQuestions 도메인 테스트")
class BalanceSessionQuestionsTest {

    @Test
    @DisplayName("정상적인 문제 목록으로 BalanceSessionQuestions 생성")
    void createBalanceSessionQuestionsTest() {
        // given
        BalanceSessionQuestion question1 = createTestQuestion(1L, 1L, 0, "질문1 A", "질문1 B");
        BalanceSessionQuestion question2 = createTestQuestion(2L, 1L, 1, "질문2 A", "질문2 B");
        List<BalanceSessionQuestion> questions = List.of(question1, question2);

        // when
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(questions);

        // then
        assertThat(sessionQuestions).isNotNull();
    }

    @Test
    @DisplayName("빈 문제 목록으로 생성 시 예외")
    void createWithEmptyListTest() {
        // given
        List<BalanceSessionQuestion> emptyQuestions = Collections.emptyList();

        // when & then
        assertThatThrownBy(() -> BalanceSessionQuestions.from(emptyQuestions))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("밸런스 게임 문제 목록이 비어있습니다");
    }

    @Test
    @DisplayName("null 문제 목록으로 생성 시 예외")
    void createWithNullListTest() {
        // when & then
        assertThatThrownBy(() -> BalanceSessionQuestions.from(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("밸런스 게임 문제 목록이 비어있습니다");
    }

    @Test
    @DisplayName("순서로 문제 조회")
    void getQuestionByOrderTest() {
        // given
        BalanceSessionQuestion question1 = createTestQuestion(1L, 1L, 0, "질문1 A", "질문1 B");
        BalanceSessionQuestion question2 = createTestQuestion(2L, 1L, 1, "질문2 A", "질문2 B");
        BalanceSessionQuestion question3 = createTestQuestion(3L, 1L, 2, "질문3 A", "질문3 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question1, question2, question3));

        // when
        BalanceSessionQuestion foundQuestion = sessionQuestions.getQuestionByOrder(1);

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundQuestion.getDisplayOrder()).isEqualTo(1);
            softly.assertThat(foundQuestion.getQuestionA()).isEqualTo("질문2 A");
            softly.assertThat(foundQuestion.getQuestionB()).isEqualTo("질문2 B");
        });
    }

    @Test
    @DisplayName("존재하지 않는 순서로 문제 조회 시 예외")
    void getQuestionByOrderNotFoundTest() {
        // given
        BalanceSessionQuestion question1 = createTestQuestion(1L, 1L, 0, "질문1 A", "질문1 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question1));

        // when & then
        assertThatThrownBy(() -> sessionQuestions.getQuestionByOrder(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 순서의 문제를 찾을 수 없습니다: 5");
    }

    @Test
    @DisplayName("문제 개수 확인")
    void getTotalCountTest() {
        // given
        BalanceSessionQuestion question1 = createTestQuestion(1L, 1L, 0, "질문1 A", "질문1 B");
        BalanceSessionQuestion question2 = createTestQuestion(2L, 1L, 1, "질문2 A", "질문2 B");
        BalanceSessionQuestion question3 = createTestQuestion(3L, 1L, 2, "질문3 A", "질문3 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question1, question2, question3));

        // when
        int totalCount = sessionQuestions.getTotalCount();

        // then
        assertThat(totalCount).isEqualTo(3);
    }

    @Test
    @DisplayName("단일 문제로 생성")
    void createWithSingleQuestionTest() {
        // given
        BalanceSessionQuestion question = createTestQuestion(1L, 1L, 0, "단일 질문 A", "단일 질문 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question));

        // when & then
        assertSoftly(softly -> {
            softly.assertThat(sessionQuestions.getTotalCount()).isEqualTo(1);
            
            BalanceSessionQuestion foundQuestion = sessionQuestions.getQuestionByOrder(0);
            softly.assertThat(foundQuestion.getQuestionA()).isEqualTo("단일 질문 A");
            softly.assertThat(foundQuestion.getQuestionB()).isEqualTo("단일 질문 B");
        });
    }

    @Test
    @DisplayName("순서가 뒤섞인 문제들로 생성 후 순서대로 조회")
    void getQuestionWithMixedOrderTest() {
        // given - 순서가 뒤섞인 문제들
        BalanceSessionQuestion question3 = createTestQuestion(3L, 1L, 2, "질문3 A", "질문3 B");
        BalanceSessionQuestion question1 = createTestQuestion(1L, 1L, 0, "질문1 A", "질문1 B");
        BalanceSessionQuestion question2 = createTestQuestion(2L, 1L, 1, "질문2 A", "질문2 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question3, question1, question2));

        // when & then - 순서대로 조회되는지 확인
        assertSoftly(softly -> {
            softly.assertThat(sessionQuestions.getQuestionByOrder(0).getQuestionA()).isEqualTo("질문1 A");
            softly.assertThat(sessionQuestions.getQuestionByOrder(1).getQuestionA()).isEqualTo("질문2 A");
            softly.assertThat(sessionQuestions.getQuestionByOrder(2).getQuestionA()).isEqualTo("질문3 A");
        });
    }

    @Test
    @DisplayName("음수 순서로 조회 시 예외")
    void getQuestionByNegativeOrderTest() {
        // given
        BalanceSessionQuestion question = createTestQuestion(1L, 1L, 0, "질문 A", "질문 B");
        BalanceSessionQuestions sessionQuestions = BalanceSessionQuestions.from(List.of(question));

        // when & then
        assertThatThrownBy(() -> sessionQuestions.getQuestionByOrder(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 순서의 문제를 찾을 수 없습니다: -1");
    }

    private BalanceSessionQuestion createTestQuestion(Long id, Long roomId, int displayOrder, String questionA, String questionB) {
        return BalanceSessionQuestion.builder()
                .roomId(roomId)
                .balanceQuestionId(id)
                .questionA(questionA)
                .questionB(questionB)
                .displayOrder(displayOrder)
                .build();
    }
} 