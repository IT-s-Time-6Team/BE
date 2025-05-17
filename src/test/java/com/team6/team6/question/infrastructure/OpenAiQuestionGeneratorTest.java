package com.team6.team6.question.infrastructure;

import com.team6.team6.global.error.exception.ExternalApiException;
import com.team6.team6.question.dto.QuestionsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiQuestionGeneratorTest {

    @Mock
    private ChatClient chatClient;

    @InjectMocks
    private OpenAiQuestionGenerator questionGenerator;

    @Test
    void 결과_반환_테스트() {
        // given
        String keyword = "LOL";
        List<String> questionsList = List.of(
                "롤에서 맞라인으로 나왔을 때 가장 싫은 챔피언은?",
                "가장 좋아하는 챔피언은 누구야?",
                "승률은 중요하게 생각해?"
        );
        QuestionsResponse questionsResponse = new QuestionsResponse(questionsList);

        // ChatClient.ChatClientRequestSpec과 ChatClient.CallResponseSpec을 모킹
        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);
        CallResponseSpec responseSpec = mock(CallResponseSpec.class);

        // when: ChatClient의 메서드 체인 모킹
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.entity(eq(QuestionsResponse.class))).thenReturn(questionsResponse);

        List<String> questions = questionGenerator.generateQuestions(keyword);

        // then
        assertSoftly(softly -> {
            softly.assertThat(questions).hasSize(3);
            softly.assertThat(questions).containsExactly(
                    "롤에서 맞라인으로 나왔을 때 가장 싫은 챔피언은?",
                    "가장 좋아하는 챔피언은 누구야?",
                    "승률은 중요하게 생각해?"
            );
        });
    }

    @Test
    void chatClient_호출_중_예외_발생_테스트() {
        // given
        String keyword = "LOL";
        ChatClientRequestSpec requestSpec = mock(ChatClientRequestSpec.class);

        // when
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenThrow(new IllegalStateException("API 실패"));

        // then
        assertThatThrownBy(() -> questionGenerator.generateQuestions(keyword))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("OpenAI 질문 생성 실패");
    }
}
