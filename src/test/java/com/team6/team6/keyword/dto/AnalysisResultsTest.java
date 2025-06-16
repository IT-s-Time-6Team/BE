package com.team6.team6.keyword.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisResultsTest {

    @Test
    void empty_메서드는_빈_결과_목록을_가진_객체를_생성한다() {
        // when
        AnalysisResults emptyResults = AnalysisResults.empty();

        // then
        assertThat(emptyResults.results()).isEmpty();
    }

    @Test
    void findSharedKeywords_필터링_정상_동작() {
        // given
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바")), // count: 3
                AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬")), // count: 2
                AnalysisResult.of("C#", Collections.singletonList("C#")) // count: 1
        );
        AnalysisResults analysisResults = AnalysisResults.of(results);

        // when
        List<String> sharedKeywords = analysisResults.findSharedKeywords(3);

        // then
        assertThat(sharedKeywords).containsExactlyInAnyOrder("Java", "JAVA", "자바");
    }

    @Test
    void findSharedKeywords_해당하는_결과가_없으면_빈_리스트_반환() {
        // given
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바")), // count: 3
                AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬")) // count: 2
        );
        AnalysisResults analysisResults = AnalysisResults.of(results);

        // when
        List<String> sharedKeywords = analysisResults.findSharedKeywords(4);

        // then
        assertThat(sharedKeywords).isEmpty();
    }

    @Test
    void findSharedKeywords_빈_결과에서_조회시_빈_리스트_반환() {
        // given
        AnalysisResults emptyResults = AnalysisResults.empty();

        // when
        List<String> sharedKeywords = emptyResults.findSharedKeywords(1);

        // then
        assertThat(sharedKeywords).isEmpty();
    }

    @Test
    void findReferenceNames_필터링_정상_동작() {
        // given
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("자바", Arrays.asList("Java", "JAVA", "자바")), // count: 3
                AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬")), // count: 2
                AnalysisResult.of("C#", Collections.singletonList("C#")) // count: 1
        );
        AnalysisResults analysisResults = AnalysisResults.of(results);

        // when
        List<String> referenceNames = analysisResults.findReferenceNames(3);

        // then
        assertThat(referenceNames).containsExactly("자바");
    }

    @Test
    void findReferenceNames_해당하는_결과가_없으면_빈_리스트_반환() {
        // given
        List<AnalysisResult> results = Arrays.asList(
                AnalysisResult.of("자바", Arrays.asList("Java", "JAVA")), // count: 2
                AnalysisResult.of("파이썬", Arrays.asList("Python", "파이썬")) // count: 2
        );
        AnalysisResults analysisResults = AnalysisResults.of(results);

        // when
        List<String> referenceNames = analysisResults.findReferenceNames(3);

        // then
        assertThat(referenceNames).isEmpty();
    }

    @Test
    void findReferenceNames_빈_결과에서_조회시_빈_리스트_반환() {
        // given
        AnalysisResults emptyResults = AnalysisResults.empty();

        // when
        List<String> referenceNames = emptyResults.findReferenceNames(1);

        // then
        assertThat(referenceNames).isEmpty();
    }
}
