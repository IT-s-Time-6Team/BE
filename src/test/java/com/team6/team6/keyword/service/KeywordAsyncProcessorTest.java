package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.GlobalKeywordManager;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeywordAsyncProcessorTest {

    @Mock
    private RoomKeywordManager roomKeywordManager;

    @Mock
    private MessagePublisher messagePublisher;

    @Mock
    private GlobalKeywordManager globalKeywordManager;

    @InjectMocks
    private KeywordAsyncProcessor keywordAsyncProcessor;

    @Test
    void processKeywordAnalysisAsync_호출시_generateQuestions_호출된다() {
        // given
        Long roomId = 2L;
        String keyword = "테스트";
        String roomKey = "roomKey";


        // keywordManager가 반환할 결과 설정
        List<String> variations = List.of("테스트", "테스트2");
        AnalysisResult result = AnalysisResult.of("테스트", variations);
        when(roomKeywordManager.addKeyword(roomId, keyword)).thenReturn(List.of(result));

        // when
        keywordAsyncProcessor.processKeywordAnalysisAsync(roomId, roomKey, keyword);

        // then
        verify(globalKeywordManager).normalizeKeyword(result, keyword);
    }
}
