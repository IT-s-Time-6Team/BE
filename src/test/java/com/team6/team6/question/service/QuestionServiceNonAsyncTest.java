package com.team6.team6.question.service;

import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.TestQuestionGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=4",
        "spring.task.execution.pool.max-size=8"
})
public class QuestionServiceNonAsyncTest {

    @TestConfiguration
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
    void generateIfNotExists_는_비동기_미설정시_바로_반환되지_않는다() {
        // given
        String keyword = "LOL";
        given(questionRepository.existsByKeyword(keyword)).willReturn(false);

        // when
        long startTime = System.currentTimeMillis();
        questionService.generateQuestions(keyword);
        long elapsed = System.currentTimeMillis() - startTime;

        // then
        assertThat(elapsed).isGreaterThanOrEqualTo(2000L); // 동기로 2초 이상 걸려야 함
    }
}
