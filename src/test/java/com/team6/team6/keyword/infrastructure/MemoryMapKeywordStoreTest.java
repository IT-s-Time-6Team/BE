package com.team6.team6.keyword.infrastructure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class MemoryMapKeywordStoreTest {

    @Test
    void 키워드_저장소_저장_조회_테스트() {
        // given
        MemoryMapKeywordStore memoryMapKeywordStore = new MemoryMapKeywordStore();
        Long roomId = 1L;
        String keyword = "testKeyword";
        memoryMapKeywordStore.saveKeyword(roomId, keyword);

        // when
        List<String> keywords = memoryMapKeywordStore.getKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(keywords).contains(keyword);
            softly.assertThat(keywords).hasSize(1);
        });
    }

    @Test
    void 키워드_순서_보장_테스트() {
        // given
        MemoryMapKeywordStore memoryMapKeywordStore = new MemoryMapKeywordStore();
        Long roomId = 1L;
        String keyword1 = "testKeyword1";
        String keyword2 = "testKeyword2";
        memoryMapKeywordStore.saveKeyword(roomId, keyword1);
        memoryMapKeywordStore.saveKeyword(roomId, keyword2);

        // when
        List<String> keywords = memoryMapKeywordStore.getKeywords(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(keywords).hasSize(2);
            softly.assertThat(keywords).containsExactly(keyword1, keyword2);
        });
    }

}
