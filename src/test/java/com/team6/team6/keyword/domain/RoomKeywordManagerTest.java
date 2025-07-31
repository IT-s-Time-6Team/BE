package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.domain.repository.GlobalKeywordRepository;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.Keyword;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomKeywordManagerTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Mock
    private KeywordSimilarityAnalyser analyser;

    @Mock
    private AnalysisResultStore analysisResultStore;

    @Mock
    private GlobalKeywordRepository globalKeywordRepository;

    @Mock
    private KeywordPreprocessor keywordPreprocessor;

    @InjectMocks
    private RoomKeywordManager roomKeywordManager;

    @Test
    void 저장소와_분석기_협력_관계_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<Keyword> keywords = List.of(
                createKeyword("AI"),
                createKeyword("Deep Learning")
        );
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        roomKeywordManager.addKeyword(roomId, keyword);

        // then
        verify(keywordRepository).findByRoomId(roomId);
        verify(analyser).analyse(keywordsInStore);
    }

    @Test
    void keyword_manager_최종_변환_테스트() {
        // given
        Long roomId = 1L;
        String keyword = "AI";

        List<Keyword> keywords = List.of(
                createKeyword("AI"),
                createKeyword("Deep Learning")
        );
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(keywordsInStore);

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = roomKeywordManager.addKeyword(roomId, keyword);

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

        List<Keyword> keywords = List.of(
                createKeyword("AI"),
                createKeyword("Deep Learning")
        );
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.of());

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = roomKeywordManager.addKeyword(roomId, keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });
    }

    @Test
    void 키워드_추가_없이_분석_테스트() {
        // given
        Long roomId = 1L;
        List<Keyword> keywords = List.of(
                createKeyword("AI"),
                createKeyword("Deep Learning")
        );
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.copyOf(keywordsInStore));

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analysisResultStore.findByRoomId(roomId)).thenReturn(List.of());
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = roomKeywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(2);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "Deep Learning");
        });

        verify(keywordRepository).findByRoomId(roomId);
        verify(analysisResultStore).findByRoomId(roomId);
        verify(analyser).analyse(keywordsInStore);
    }

    @Test
    void analyzeAndSave_정상_동작_테스트() {
        // given
        Long roomId = 1L;
        List<Keyword> keywords = List.of(
                createKeyword("AI"),
                createKeyword("Deep Learning")
        );
        List<String> keywordsInStore = List.of("AI", "Deep Learning");
        List<List<String>> expectedResult = List.of(List.copyOf(keywordsInStore));

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analysisResultStore.findByRoomId(roomId)).thenReturn(List.of());
        when(analyser.analyse(keywordsInStore)).thenReturn(expectedResult);

        // when
        List<AnalysisResult> results = roomKeywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(2);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "Deep Learning");
        });

        verify(keywordRepository).findByRoomId(roomId);
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
        List<AnalysisResult> results = roomKeywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEqualTo(storedResults);
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordRepository, analyser);
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
        List<AnalysisResult> results = roomKeywordManager.getAnalysisResult(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEqualTo(expectedResults);
            softly.assertThat(results).hasSize(2);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(1).referenceName()).isEqualTo("Java");
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordRepository, analyser);
    }

    @Test
    void getAnalysisResult_빈_결과_반환_테스트() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> emptyResults = List.of();

        when(analysisResultStore.findByRoomId(roomId)).thenReturn(emptyResults);

        // when
        List<AnalysisResult> results = roomKeywordManager.getAnalysisResult(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).isEmpty();
        });

        verify(analysisResultStore).findByRoomId(roomId);
        verifyNoInteractions(keywordRepository, analyser);
    }

    @Test
    void addKeyword_기존_그룹에_추가_성공_테스트() {
        // given
        Long roomId = 1L;
        String newKeyword = "머신러닝";
        String preprocessedKeyword = "머신러닝";
        List<String> referenceNames = List.of("AI");
        List<String> preprocessedReferenceNames = List.of("ai");

        GlobalKeyword matchingKeyword = GlobalKeyword.create("ai", null);

        List<AnalysisResult> existingResults = List.of(
                AnalysisResult.of("AI", List.of("AI", "인공지능"))
        );

        when(analysisResultStore.findReferenceNamesByRoomId(roomId, 1)).thenReturn(referenceNames);
        when(keywordPreprocessor.preprocess("AI")).thenReturn("ai");
        when(keywordPreprocessor.preprocess(newKeyword)).thenReturn(preprocessedKeyword);
        when(globalKeywordRepository.findByKeywordInAndSameGroupAs(preprocessedReferenceNames, preprocessedKeyword))
                .thenReturn(Optional.of(matchingKeyword));
        when(analysisResultStore.findByRoomId(roomId)).thenReturn(existingResults);

        // when
        List<AnalysisResult> results = roomKeywordManager.addKeyword(roomId, newKeyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(3);
            softly.assertThat(results.get(0).variations()).containsExactly("AI", "인공지능", "머신러닝");
        });

        verify(analysisResultStore).save(eq(roomId), any());
        verifyNoInteractions(analyser);
    }

    @Test
    void addKeyword_참조_키워드_없어서_새로_분석_테스트() {
        // given
        Long roomId = 1L;
        String newKeyword = "Python";
        List<String> emptyReferenceNames = List.of();

        List<Keyword> keywords = List.of(createKeyword("Python"));
        List<String> keywordsInStore = List.of("Python");

        when(analysisResultStore.findReferenceNamesByRoomId(roomId, 1)).thenReturn(emptyReferenceNames);
        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);

        // when
        List<AnalysisResult> results = roomKeywordManager.addKeyword(roomId, newKeyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("Python");
            softly.assertThat(results.get(0).count()).isEqualTo(1);
            softly.assertThat(results.get(0).variations()).containsExactly("Python");
        });

        verifyNoInteractions(globalKeywordRepository, keywordPreprocessor, analyser);
        verify(analysisResultStore).save(eq(roomId), any());
    }

    @Test
    void analyzeAndSave_키워드_1개_그룹화_생략_테스트() {
        // given
        Long roomId = 1L;
        List<Keyword> keywords = List.of(createKeyword("AI"));

        when(keywordRepository.findByRoomId(roomId)).thenReturn(keywords);
        when(analysisResultStore.findByRoomId(roomId)).thenReturn(List.of());

        // when
        List<AnalysisResult> results = roomKeywordManager.analyzeKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("AI");
            softly.assertThat(results.get(0).count()).isEqualTo(1);
            softly.assertThat(results.get(0).variations()).containsExactly("AI");
        });

        verify(keywordRepository).findByRoomId(roomId);
        verify(analysisResultStore).save(eq(roomId), any());
        verifyNoInteractions(analyser);
    }

    private Keyword createKeyword(String keywordValue) {
        return Keyword.builder()
                .keyword(keywordValue)
                .build();
    }
}
