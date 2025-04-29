package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.exception.exceptions.AiResponseParsingException;
import com.team6.team6.keyword.exception.exceptions.EmptyKeywordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec response = mock(CallResponseSpec.class);

        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(response);
        when(response.entity((ParameterizedTypeReference<Object>) any())).thenReturn(expected);

        // when
        List<List<String>> result = analyser.analyse(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 입력_키워드가_비어_있으면_예외_테스트() {
        assertThatThrownBy(() -> analyser.analyse(List.of()))
                .isInstanceOf(EmptyKeywordException.class);
    }

    @Test
    void ChatClient_내부_예외_발생_시_테스트() {
        when(chatClient.prompt(any(Prompt.class))).thenThrow(new RuntimeException("error"));

        assertThatThrownBy(() -> analyser.analyse(List.of("롤", "lol")))
                .isInstanceOf(AiResponseParsingException.class)
                .hasCauseInstanceOf(RuntimeException.class);
    }
}

