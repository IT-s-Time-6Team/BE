package com.team6.team6.keyword.service;

import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.dto.KeywordAddServiceReq;
import com.team6.team6.keyword.entity.Keyword;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class KeywordServiceDBTest {

    @Autowired
    private KeywordRepository keywordRepository;

    @Autowired
    private KeywordService keywordService;

    private static final Long ROOM_ID = 1L;
    private static final String KEYWORD_TEXT = "테스트키워드";
    private static final Long MEMBER_ID = 100L;


    @Test
    void 키워드_추가시_키워드가_저장되고_반환된다() {
        // given
        KeywordAddServiceReq req = KeywordAddServiceReq.of(KEYWORD_TEXT, ROOM_ID, MEMBER_ID);

        // when
        Keyword result = keywordService.addKeyword(req);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result.getId()).isNotNull();
            softly.assertThat(result.getKeyword()).isEqualTo(KEYWORD_TEXT);
            softly.assertThat(result.getRoomId()).isEqualTo(ROOM_ID);
            softly.assertThat(result.getMemberId()).isEqualTo(MEMBER_ID);
        });
    }

    @Test
    void 같은_사용자_같은_키워드_추가가능() {
        // given
        KeywordAddServiceReq req = KeywordAddServiceReq.of(KEYWORD_TEXT, ROOM_ID, MEMBER_ID);
        keywordService.addKeyword(req);

        // when
        KeywordAddServiceReq duplicateReq = KeywordAddServiceReq.of(KEYWORD_TEXT, ROOM_ID, MEMBER_ID);
        Keyword duplicateResult = keywordService.addKeyword(duplicateReq);
        List<Keyword> savedKeyword = keywordRepository.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(duplicateResult).isNotNull();
            softly.assertThat(savedKeyword.size()).isEqualTo(2);
            softly.assertThat(savedKeyword.get(0).getKeyword()).isEqualTo(savedKeyword.get(1).getKeyword());
            softly.assertThat(savedKeyword.get(0).getRoomId()).isEqualTo(savedKeyword.get(1).getRoomId());
            softly.assertThat(savedKeyword.get(0).getMemberId()).isEqualTo(savedKeyword.get(1).getMemberId());
        });
    }
}

