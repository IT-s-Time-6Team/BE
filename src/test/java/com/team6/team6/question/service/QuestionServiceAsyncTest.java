package com.team6.team6.question.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.SimulatedLatencyQuestionGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=4",
        "spring.task.execution.pool.max-size=8"
})
@ActiveProfiles("test")
class QuestionServiceAsyncTest {

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
                ready.countDown(); // 준비 완료
                start.await();     // 시작 신호 대기
                questionService.generateQuestions(keyword);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // when - 두 스레드 동시에 실행
        executor.submit(task);
        executor.submit(task);

        ready.await(); // 두 스레드가 준비될 때까지 대기
        start.countDown(); // 동시에 시작
        executor.shutdown();

        // then - saveAll이 단 한 번만 호출되었는지 확인 (비동기 작업 완료까지 대기)
        await().atMost(3, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(questionRepository, times(1)).saveAll(anyList()));
    }

    @Test
    void Question이_없을_때_예외_처리_테스트() {
        // given
        String keyword = "LOL";
        given(questionRepository.findAllByKeyword(keyword)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> questionService.getRandomQuestions(keyword))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("해당 키워드에 대한 질문이 존재하지 않습니다");
    }

    @TestConfiguration
    @EnableAsync
    static class TestConfig {
        @Bean
        public QuestionGenerator questionGenerator() {
            return new SimulatedLatencyQuestionGenerator();
        }
    }
}

