package com.team6.team6.question.service;

import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.TestQuestionGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=4",
        "spring.task.execution.pool.max-size=8"
})
class QuestionServiceAsyncTest {

    @TestConfiguration
    @EnableAsync
    static class TestConfig {
        @Bean
        public QuestionGenerator questionGenerator() {
            return new TestQuestionGenerator();
        }
    }

    @MockitoBean
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionService questionService;

    @Test
    void generateQuestions는_비동기로_즉시_반환된다() {
        // given
        String keyword = "LOL";
        given(questionRepository.existsByKeyword(keyword)).willReturn(false);

        // when
        long startTime = System.currentTimeMillis();
        questionService.generateQuestions(keyword);
        long elapsed = System.currentTimeMillis() - startTime;

        // then
        assertThat(elapsed).isLessThan(500L); // 0.5초 이내에 반환되어야 함
    }
}

