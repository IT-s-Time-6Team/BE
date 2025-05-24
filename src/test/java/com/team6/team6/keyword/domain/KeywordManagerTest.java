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
    private KeywordStore keywordStore;

    @Mock
    private KeywordSimilarityAnalyser analyser;

    @Mock
    private AnalysisResultStore analysisResultStore;

    @InjectMocks
    private KeywordManager keywordManager;

    @Test
    void 저장소와_분석기_협력_관계_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        doNothing().when(keywordStore).saveKeyword(roomId, keyword);
        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        keywordManager.addKeyword(roomId, keyword);

        // then
        verify(keywordStore).saveKeyword(roomId, keyword);
        verify(keywordStore).getKeywords(roomId);
        verify(analyser).analyse(keywordsInStore);
    }

    @Test
    void keyword_manager_최종_변환_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        doNothing().when(keywordStore).saveKeyword(roomId, keyword);
        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
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

        doNothing().when(keywordStore).saveKeyword(roomId, keyword);
        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.addKeyword(roomId, keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });
    }

    @Test
    void 키워드_추가_없이_분석_테스트() {
        // given
        Long roomId = 1L;
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.copyOf(keywordsInStore));

        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analysisResultStore.findByRoomId(roomId)).thenReturn(List.of());
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(2);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "Deep Learning");
        });

        verify(keywordStore).getKeywords(roomId);
        verify(analysisResultStore).findByRoomId(roomId);
        verify(analyser).analyse(keywordsInStore);
    }

    @Test
    void 키워드_추가_없이_분석_빈_그룹_테스트() {
        // given
        Long roomId = 1L;
        List<String> keywordsInStore = List.of();
        List<List<String>> expectedResult = List.of();

        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });
    }

    @Test
    void analyzeAndSave_정상_동작_테스트() {
        // given
        Long roomId = 1L;
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.copyOf(keywordsInStore));

        when(keywordStore.getKeywords(roomId)).thenReturn(keywordsInStore);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = keywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(2);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "Deep Learning");
        });

        verify(keywordStore).getKeywords(roomId);
        verify(analyser).analyse(keywordsInStore);
        verify(analysisResultStore).save(roomId, results);
    }

    @Test
    void analyzeKeywords_저장된_결과_반환_테스트() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> storedResults = List.of(
                AnalysisResult.of("AI", List.of("AI", "Deep Learning"))
        );

        when(analysisResultStore.findByRoomId(roomId)).thenReturn(storedResults);

        // when
        List<AnalysisResult> results = keywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEqualTo(storedResults);
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordStore, analyser);
    }

    @Test
    void getAnalysisResult_저장된_결과_반환_테스트() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> expectedResults = List.of(
                AnalysisResult.of("AI", List.of("AI", "Deep Learning")),
                AnalysisResult.of("Java", List.of("Java", "JavaScript"))
        );

        when(analysisResultStore.findByRoomId(roomId)).thenReturn(expectedResults);

        // when
        List<AnalysisResult> results = keywordManager.getAnalysisResult(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEqualTo(expectedResults);
            softly.assertThat(results).hasSize(2);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(1).referenceName()).isEqualTo("Java");
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordStore, analyser);
    }

    @Test
    void getAnalysisResult_빈_결과_반환_테스트() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> emptyResults = List.of();

        when(analysisResultStore.findByRoomId(roomId)).thenReturn(emptyResults);

        // when
        List<AnalysisResult> results = keywordManager.getAnalysisResult(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordStore, analyser);
    }
}
