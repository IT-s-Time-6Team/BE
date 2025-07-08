package com.team6.team6.keyword.domain;

import com.team6.team6.keyword.domain.repository.GlobalKeywordRepository;
import com.team6.team6.keyword.domain.repository.KeywordGroupRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.entity.GlobalKeyword;
import com.team6.team6.keyword.entity.KeywordGroup;
import com.team6.team6.question.service.QuestionGenerationFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalKeywordManagerTest {

    // KeywordPreprocessor는 실제 구현체 사용
    private KeywordPreprocessor keywordPreprocessor = new KeywordPreprocessor();

    @Mock
    private GlobalKeywordRepository globalKeywordRepository;

    @Mock
    private KeywordGroupRepository keywordGroupRepository;

    @Mock
    private QuestionGenerationFacade generationFacade;

    private GlobalKeywordManager globalKeywordManager;

    @Captor
    private ArgumentCaptor<GlobalKeyword> keywordCaptor;

    @Captor
    private ArgumentCaptor<KeywordGroup> groupCaptor;

    @BeforeEach
    void setUp() {
        // 실제 KeywordPreprocessor와 모킹된 리포지토리들을 주입
        globalKeywordManager = new GlobalKeywordManager(
                keywordPreprocessor,
                globalKeywordRepository,
                keywordGroupRepository,
                generationFacade
        );
    }

    @Test
    void new_keyword가_이미_처리된_키워드라면_메서드_종료() {
        // given
        AnalysisResult result = AnalysisResult.of("Java", Arrays.asList("Java", "java", "JAVA"));
        String newKeyword = "Java";

        // when
        globalKeywordManager.normalizeKeyword(result, newKeyword);

        // then
        verify(globalKeywordRepository, never()).findByKeyword(anyString());
        verify(generationFacade, never()).generateQuestions(anyString(), any(KeywordGroup.class));
    }

    @Test
    void new_keyword가_global_keyword에_존재하지만_같은_그룹내_다른_키워드가_있을_때_테스트() {
        // given
        AnalysisResult result = AnalysisResult.of("Spring", Arrays.asList("Spring", "spring boot"));
        String newKeyword = "java";

        // java 키워드는 존재하지 않음
        when(globalKeywordRepository.findByKeyword("java")).thenReturn(Optional.empty());

        // spring 키워드는 그룹을 가지고 있음
        KeywordGroup frameworkGroup = KeywordGroup.create("spring");
        GlobalKeyword springKeyword = GlobalKeyword.create("spring", frameworkGroup);
        // 실제 전처리된 키워드로 모킹
        when(globalKeywordRepository.findByKeywordIn(Arrays.asList("spring", "springboot")))
                .thenReturn(List.of(springKeyword));

        // when
        globalKeywordManager.normalizeKeyword(result, newKeyword);

        // then
        verify(globalKeywordRepository).save(keywordCaptor.capture());
        GlobalKeyword savedKeyword = keywordCaptor.getValue();
        assertSoftly(softly -> {
            softly.assertThat(savedKeyword.getKeyword()).isEqualTo("java");
            softly.assertThat(savedKeyword.getKeywordGroup()).isEqualTo(frameworkGroup);
        });
        verify(generationFacade, never()).generateQuestions(anyString(), any(KeywordGroup.class));
    }

    @Test
    void 새_키워드가_이미_존재하고_모든_키워드가_동일_그룹인_경우_테스트() {
        // given
        AnalysisResult result = AnalysisResult.of("Spring", Arrays.asList("Spring", "spring boot"));
        String newKeyword = "java";

        // 같은 그룹에 속한 키워드들
        KeywordGroup programmingGroup = KeywordGroup.create("spring");

        // java 키워드는 이미 존재
        GlobalKeyword existingJavaKeyword = GlobalKeyword.create("java", programmingGroup);
        when(globalKeywordRepository.findByKeyword("java")).thenReturn(Optional.of(existingJavaKeyword));

        // Spring 키워드도 같은 그룹에 존재
        GlobalKeyword springKeyword = GlobalKeyword.create("spring", programmingGroup);
        when(globalKeywordRepository.findByKeywordIn(Arrays.asList("spring", "springboot")))
                .thenReturn(List.of(springKeyword));

        // when
        globalKeywordManager.normalizeKeyword(result, newKeyword);

        // then
        // 같은 그룹이므로 그룹 병합이 발생하지 않음
        verify(globalKeywordRepository, never()).bulkUpdateKeywordGroups(any(), anyCollection());
        // 키워드 저장도 발생하지 않음
        verify(globalKeywordRepository, never()).save(any(GlobalKeyword.class));
        verify(generationFacade, never()).generateQuestions(anyString(), any(KeywordGroup.class));
    }

    @Test
    void new_keyword가_global_keyword에_이미_존재하고_충돌이_일어났을_때_테스트() {
        // given
        AnalysisResult result = AnalysisResult.of("Spring", Arrays.asList("Spring", "spring boot"));
        String newKeyword = "java";

        // java 키워드는 이미 존재하며 java 그룹에 속함
        KeywordGroup javaGroup = KeywordGroup.create("java");
        GlobalKeyword existingKeyword = GlobalKeyword.create("java", javaGroup);
        when(globalKeywordRepository.findByKeyword("java")).thenReturn(Optional.of(existingKeyword));

        // spring 관련 키워드들은 다른 그룹에 속함
        KeywordGroup springGroup = KeywordGroup.create("spring");
        GlobalKeyword springKeyword = GlobalKeyword.create("spring", springGroup);
        when(globalKeywordRepository.findByKeywordIn(Arrays.asList("spring", "springboot")))
                .thenReturn(List.of(springKeyword));

        // when
        globalKeywordManager.normalizeKeyword(result, newKeyword);

        // then
        verify(globalKeywordRepository).bulkUpdateKeywordGroups(javaGroup, Set.of(springGroup));
        verify(generationFacade, never()).generateQuestions(anyString(), any(KeywordGroup.class));
    }
}
