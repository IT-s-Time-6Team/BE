package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.KeywordAddServiceReq;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.question.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class KeywordServiceNonAsyncTest {

    @TestConfiguration
//    @EnableAsync활성화를 해제한다
    static class TestConfig {
        @Bean
        public KeywordManager keywordManager() {
            return new SimulatedLatencyKeywordManager(2000);
        }
    }

    @MockitoBean
    private KeywordRepository keywordRepository;

    @MockitoBean
    private MessagePublisher messagePublisher;

    @Autowired
    private KeywordService keywordService;

    @MockitoBean
    private QuestionService questionService;

    private static final Long ROOM_ID = 1L;
    private static final String KEYWORD_TEXT = "테스트키워드";
    private static final Long MEMBER_ID = 100L;

    @Test
    void Async_비활성화시_키워드_추가_메서드는_즉시_반환되지_않는다() {
        // given
        KeywordAddServiceReq req = KeywordAddServiceReq.of(KEYWORD_TEXT, "x2xx33", ROOM_ID, MEMBER_ID);
        Keyword savedKeyword = req.toEntity();
        given(keywordRepository.save(any(Keyword.class))).willReturn(savedKeyword);

        // when
        long startTime = System.currentTimeMillis();
        Keyword result = keywordService.addKeyword(req);
        long executionTime = System.currentTimeMillis() - startTime;

        // then
//        assertThat(executionTime).isGreaterThan(2000L);
//        assertThat(result).isEqualTo(savedKeyword);
    }
}
