package com.team6.team6.keyword.infrastructure;

import com.team6.team6.global.error.exception.ExternalApiException;
import com.team6.team6.keyword.dto.KeywordGroupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiKeywordSimilarityAnalyserTest {

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private OpenAiKeywordSimilarityAnalyser analyser;

    @Test
    void 그룹핑된_결과를_반환_테스트() {
        // given
        List<String> input = List.of("lol", "리그오브레전드");
        List<List<String>> expected = List.of(List.of("lol", "리그오브레전드"));
        KeywordGroupResponse response = new KeywordGroupResponse(expected);

        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec responseSpec = mock(CallResponseSpec.class);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(eq(KeywordGroupResponse.class))).thenReturn(response);

        // when
        List<List<String>> result = analyser.analyse(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 입력_키워드가_비어_있으면_빈_리스트_반환_테스트() {
        // given
        List<String> input = List.of();

        // when
        List<List<String>> result = analyser.analyse(input);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void ChatClient_내부_예외_발생_시_테스트() {
        // given
        List<String> input = List.of("롤", "lol");
        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);

        // when
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new RuntimeException("error"));

        // then
        assertThatThrownBy(() -> analyser.analyse(input))
                .isInstanceOf(ExternalApiException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }
}
