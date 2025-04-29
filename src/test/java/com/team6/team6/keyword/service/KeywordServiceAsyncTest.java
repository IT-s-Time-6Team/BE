package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordAddServiceReq;
import com.team6.team6.keyword.entity.Keyword;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.task.execution.pool.core-size=4",
        "spring.task.execution.pool.max-size=8"
})
class KeywordServiceAsyncTest {

    @TestConfiguration
    @EnableAsync
    static class TestConfig {
        @Bean
        public KeywordManager keywordManager() {
            return new KeywordManager() {
                @Override
                public List<AnalysisResult> addKeyword(Long roomId, String keyword) {
                    try {
                        Thread.sleep(2000);
                        List<String> variations = List.of("분석결과1", "분석결과2");
                        return List.of(AnalysisResult.of("분석결과", variations));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("키워드 분석 중 인터럽트 발생", e);
                    }
                }
            };
        }
    }

    @MockitoBean
    private KeywordRepository keywordRepository;

    @MockitoBean
    private MessagePublisher messagePublisher;

    @Autowired
    private KeywordService keywordService;

    private static final Long ROOM_ID = 1L;
    private static final String KEYWORD_TEXT = "테스트키워드";
    private static final Long MEMBER_ID = 100L;

    @Test
    void 키워드_추가_메서드는_즉시_반환된다() {
        // given
        KeywordAddServiceReq req = KeywordAddServiceReq.of(KEYWORD_TEXT, ROOM_ID, MEMBER_ID);
        Keyword savedKeyword = req.toEntity();
        given(keywordRepository.save(any(Keyword.class))).willReturn(savedKeyword);

        // when
        long startTime = System.currentTimeMillis();
        Keyword result = keywordService.addKeyword(req);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
        assertThat(executionTime).isLessThan(500);
        assertThat(result).isEqualTo(savedKeyword);
    }
}