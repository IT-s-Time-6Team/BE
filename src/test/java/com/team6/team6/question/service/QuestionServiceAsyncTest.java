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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=4",
        "spring.task.execution.pool.max-size=8"
})
@ActiveProfiles("test")
class QuestionServiceAsyncTest {

    @TestConfiguration
    @EnableAsync
    static class TestConfig {
        @Bean
        public QuestionGenerator questionGenerator() {
            return new TestQuestionGenerator();
        }
    }

    @MockitoSpyBean
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

    @Test
    void 동시에_두_요청이_와도_질문은_한번만_생성된다() throws Exception {
        // given
        String keyword = "LOL";
        given(questionRepository.existsByKeyword(keyword)).willReturn(false);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Runnable task = () -> {
            try {
                ready.countDown();
                start.await();
                questionService.generateQuestions(keyword);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // when - 두 스레드 동시에 실행
        executor.submit(task);
        executor.submit(task);

        ready.await();  // 두 스레드 준비 대기
        start.countDown(); // 동시에 실행 시작
        executor.awaitTermination(3, TimeUnit.SECONDS);

        // then
        verify(questionRepository, times(1)).saveAll(anyList());
        executor.shutdown();
    }
}

