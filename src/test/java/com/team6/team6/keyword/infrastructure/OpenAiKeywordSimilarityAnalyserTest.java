package com.team6.team6.keyword.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
class OpenAiKeywordSimilarityAnalyserTest {

    @Autowired
    private OpenAiKeywordSimilarityAnalyser openAiKeywordSimilarityAnalyser;

    @Test
    void 그룹핑_테스트() {
        // Given
        List<String> keywords = List.of("롤", "리그오브레전드", "LOL", "리그 오브 레전드");

        // When
        List<List<String>> result = openAiKeywordSimilarityAnalyser.analyse(keywords);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.get(0)).containsExactly("롤", "리그오브레전드", "LOL", "리그 오브 레전드");
        });
    }
}
