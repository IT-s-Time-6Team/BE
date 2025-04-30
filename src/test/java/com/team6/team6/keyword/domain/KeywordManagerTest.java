package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.dto.AnalysisResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordManagerTest {

    @Mock
    private KeywordStore store;

    @Mock
    private KeywordSimilarityAnalyser analyser;

    @InjectMocks
    private KeywordManager keywordManager;

    @Test
    void 저장소와_분석기_협력_관계_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        doNothing().when(store).saveKeyword(roomId, keyword);
        when(store.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        keywordManager.addKeyword(roomId, keyword);

        // then
        verify(store).saveKeyword(roomId, keyword);
        verify(store).getKeywords(roomId);
        verify(analyser).analyse(keywordsInStore);
    }

    @Test
    void keyword_manager_최종_변환_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        doNothing().when(store).saveKeyword(roomId, keyword);
        when(store.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.addKeyword(roomId, keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(2);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "Deep Learning");
        });
    }

    @Test
    void keyword_manager_빈_그룹_제외_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.of());

        doNothing().when(store).saveKeyword(roomId, keyword);
        when(store.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.addKeyword(roomId, keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });
    }
}
