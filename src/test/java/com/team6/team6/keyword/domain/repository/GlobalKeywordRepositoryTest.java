package com.team6.team6.keyword.domain.repository;

import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.KeywordGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
@ActiveProfiles("test")
class GlobalKeywordRepositoryTest {

    @Autowired
    private GlobalKeywordRepository globalKeywordRepository;
    @Autowired
    private KeywordGroupRepository keywordGroupRepository;

    @Test
    void 키워드_목록으로_여러_글로벌_키워드_조회_테스트() {
        // given
        GlobalKeyword keyword1 = GlobalKeyword.create("자바", null);
        GlobalKeyword keyword2 = GlobalKeyword.create("스프링", null);
        GlobalKeyword keyword3 = GlobalKeyword.create("JPA", null);
        globalKeywordRepository.saveAll(Arrays.asList(keyword1, keyword2, keyword3));

        // when
        List<GlobalKeyword> foundKeywords = globalKeywordRepository.findByKeywordIn(
                Arrays.asList("자바", "스프링"));

        // then
        assertThat(foundKeywords).hasSize(2);
        assertThat(foundKeywords).extracting("keyword")
                .containsExactlyInAnyOrder("자바", "스프링");
    }

    @Test
    void 키워드로_단일_글로벌_키워드_조회_테스트() {
        // given
        GlobalKeyword keyword = GlobalKeyword.create("자바", null);
        globalKeywordRepository.save(keyword);

        // when
        Optional<GlobalKeyword> foundKeyword = globalKeywordRepository.findByKeyword("자바");

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundKeyword).isPresent();
            softly.assertThat(foundKeyword.get().getKeyword()).isEqualTo("자바");
        });
    }

    @Test
    void 여러_키워드_그룹_병합_테스트() {
        // given
        KeywordGroup oldGroup1 = KeywordGroup.create("그룹1");
        KeywordGroup oldGroup2 = KeywordGroup.create("그룹2");
        KeywordGroup targetGroup = KeywordGroup.create("타겟그룹");

        GlobalKeyword keyword1 = GlobalKeyword.create("키워드1", oldGroup1);
        GlobalKeyword keyword2 = GlobalKeyword.create("키워드2", oldGroup1);
        GlobalKeyword keyword3 = GlobalKeyword.create("키워드3", oldGroup2);
        GlobalKeyword keyword4 = GlobalKeyword.create("키워드4", targetGroup);

        globalKeywordRepository.saveAll(Arrays.asList(keyword1, keyword2, keyword3, keyword4));
        keywordGroupRepository.saveAll(Arrays.asList(oldGroup1, oldGroup2, targetGroup));

        // when
        int updatedCount = globalKeywordRepository.bulkUpdateKeywordGroups(
                targetGroup,
                Set.of(oldGroup1, oldGroup2)
        );

        // then
        List<GlobalKeyword> allKeywords = globalKeywordRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(updatedCount).isEqualTo(3); // 3개의 레코드가 업데이트되어야 함
            allKeywords.forEach(keyword ->
                    assertThat(keyword.getKeywordGroup().getRepresentativeKeyword()).isEqualTo(targetGroup.getRepresentativeKeyword()));
        });
    }
}
