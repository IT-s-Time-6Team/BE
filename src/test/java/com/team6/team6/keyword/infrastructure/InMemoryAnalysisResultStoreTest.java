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
    void 여러_분석_결과를_저장할_수_있다() {
        // given
        Long roomId = 1L;
        AnalysisResult result1 = AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바"));
        AnalysisResult result2 = AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬"));
        // when
        store.save(roomId, Arrays.asList(result1, result2));

        // then
        List<AnalysisResult> results = store.findByRoomId(roomId);
        assertThat(results).hasSize(2);
    }

    @Test
    void 공유_키워드_목록을_중복_없이_가져올_수_있다() {
        // given
        Long roomId = 1L;
        AnalysisResult result1 = AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바"));
        AnalysisResult result2 = AnalysisResult.of("파이썬", Arrays.asList("Python", "python", "파이썬"));
        AnalysisResult result3 = AnalysisResult.of("자바스크립트", Arrays.asList("JavaScript", "JS"));

        store.save(roomId, Arrays.asList(result1, result2, result3));

        // when
        List<String> sharedKeywords = store.findSharedKeywordsByRoomId(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(sharedKeywords).hasSize(8);
            softly.assertThat(sharedKeywords).containsExactlyInAnyOrder(
                    "Java", "JAVA", "자바", "Python", "python", "파이썬", "JavaScript", "JS"
            );
        });
    }

    @Test
    void 공감된_키워드들을_중복_없이_가져올_수_있다() {
        // given
        Long roomId = 1L;
        AnalysisResult result1 = AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바"));
        AnalysisResult result2 = AnalysisResult.of("파이썬", Arrays.asList("Python", "python", "파이썬"));
        AnalysisResult result3 = AnalysisResult.of("자바스크립트", Arrays.asList("JavaScript", "JS"));

        store.save(roomId, Arrays.asList(result1, result2, result3));

        // when
        List<String> sharedKeywords = store.findReferenceNamesByRoomId(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(sharedKeywords).hasSize(3);
            softly.assertThat(sharedKeywords).containsExactlyInAnyOrder(
                    "자바", "파이썬", "자바스크립트"
            );
        });
    }

    @Test
    void 서로_다른_방_ID의_결과는_독립적으로_관리된다() {
        // given
        Long roomId1 = 1L;
        Long roomId2 = 2L;

        AnalysisResult result1 = AnalysisResult.of("자바", Arrays.asList("Java", "자바"));
        AnalysisResult result2 = AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬"));

        // when
        store.save(roomId1, List.of(result1));
        store.save(roomId2, List.of(result2));

        // then
        List<AnalysisResult> results1 = store.findByRoomId(roomId1);
        List<AnalysisResult> results2 = store.findByRoomId(roomId2);

        assertSoftly(softly -> {
            softly.assertThat(results1).hasSize(1);
            softly.assertThat(results1.get(0).referenceName()).isEqualTo("자바");

            softly.assertThat(results2).hasSize(1);
            softly.assertThat(results2.get(0).referenceName()).isEqualTo("파이썬");
        });
    }
}
