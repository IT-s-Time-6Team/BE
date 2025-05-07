package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.dto.AnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class InMemoryAnalysisResultStoreTest {

    private InMemoryAnalysisResultStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryAnalysisResultStore();
    }

    @Test
    void 분석_결과를_저장하고_조회할_수_있다() {
        // given
        Long roomId = 1L;
        AnalysisResult result = AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바"));

        // when
        store.save(roomId, List.of(result));
        List<AnalysisResult> savedResults = store.findByRoomId(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedResults).hasSize(1);
            softly.assertThat(savedResults.get(0).referenceName()).isEqualTo("자바");
            softly.assertThat(savedResults.get(0).variations()).containsExactly("Java", "JAVA", "자바");
        });
    }

    @Test
    void 존재하지_않는_방_ID로_조회하면_빈_리스트가_반환된다() {
        // given
        Long nonExistentRoomId = 999L;

        // when
        List<AnalysisResult> results = store.findByRoomId(nonExistentRoomId);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void 기존_결과가_있을_때_저장하면_교체된다() {
        // given
        Long roomId = 1L;
        AnalysisResult firstResult = AnalysisResult.of("파이썬", Arrays.asList("파이썬"));
        store.save(roomId, List.of(firstResult));

        // when
        AnalysisResult secondResult = AnalysisResult.of("파이썬", Arrays.asList("파이썬", "Python"));
        store.save(roomId, List.of(secondResult));

        // then
        List<AnalysisResult> results = store.findByRoomId(roomId);
        assertSoftly(softly -> {
            softly.assertThat(results).hasSize(1);
            softly.assertThat(results.get(0).referenceName()).isEqualTo("파이썬");
            softly.assertThat(results.get(0).variations()).containsExactly("파이썬", "Python");
        });
    }

    @Test
    void findSharedKeywordsByRoomId_필터링_성공() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("var1", Arrays.asList("var1", "var2")), // count: 2
                AnalysisResult.of("var3", Arrays.asList("var3", "var4", "var5")), // count: 3
                AnalysisResult.of("var6", Arrays.asList("var6")) // count: 1
        );
        store.save(roomId, results);

        // when
        List<String> sharedKeywords = store.findSharedKeywordsByRoomId(roomId, 3);

        // then
        assertThat(sharedKeywords).containsExactlyInAnyOrder("var3", "var4", "var5");
    }

    @Test
    void findReferenceNamesByRoomId_필터링_성공() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("var1", Arrays.asList("var1", "var2")), // count: 2
                AnalysisResult.of("var3", Arrays.asList("var3", "var4", "var5")), // count: 3
                AnalysisResult.of("var6", Arrays.asList("var6")) // count: 1
        );
        store.save(roomId, results);

        // when
        List<String> referenceNames = store.findReferenceNamesByRoomId(roomId, 3);

        // then
        assertThat(referenceNames).containsExactly("var3");
    }

    @Test
    void findSharedKeywordsByRoomId_빈_결과() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("var1", Arrays.asList("var1", "var2")), // count: 2
                AnalysisResult.of("var6", Arrays.asList("var6")) // count: 1
        );
        store.save(roomId, results);

        // when
        List<String> sharedKeywords = store.findSharedKeywordsByRoomId(roomId, 3);

        // then
        assertThat(sharedKeywords).isEmpty();
    }

    @Test
    void findReferenceNamesByRoomId_빈_결과() {
        // given
        Long roomId = 1L;
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("var1", Arrays.asList("var1", "var2")), // count: 2
                AnalysisResult.of("var6", Arrays.asList("var6")) // count: 1
        );
        store.save(roomId, results);

        // when
        List<String> referenceNames = store.findReferenceNamesByRoomId(roomId, 3);

        // then
        assertThat(referenceNames).isEmpty();
    }
}
