package com.team6.team6.question.infrastructure;

import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.dto.QuestionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiQuestionGenerator implements QuestionGenerator {

    private final ChatClient chatClient;

    private static final PromptTemplate PROMPT_TEMPLATE = new PromptTemplate("""
            키워드: {keyword}
            
            이 키워드를 주제로, 처음 만난 사람들이 편하게 이야기 나눌 수 있도록 대화형 질문 20개를 만들어줘.
            
            아래 기준을 지켜줘:
            - 너무 딱딱하거나 교과서적인 말투는 피하고, 자연스럽고 구어체로 작성해
            - 경험, 취향, 감정을 물을 수 있는 질문이 좋고, '너라면 어때?' 식의 접근도 괜찮아
            - 질문 하나당 한 줄로 만들어줘
            - 숫자나 기호 없이 질문 내용만 출력해
            - 결과를 JSON 형식의 질문 목록으로 반환해줘, "questions" 키에 질문 배열이 담겨야 함
            
            예시 응답 형식:
            {
              "questions": [
                "롤에서 맞라인으로 나왔을 때 가장 싫은 챔피언은?",
                "첫 번째 롤 챔피언은 뭐였어?"
              ]
            }
            """
    );

    @Override
    public List<String> generateQuestions(String keyword) {
        Prompt prompt = PROMPT_TEMPLATE.create(Map.of("keyword", keyword));

        try {
            QuestionsResponse response = chatClient.prompt(prompt)
                    .call()
                    .entity(QuestionsResponse.class);

            return parseQuestions(response.questions());
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 질문 생성 실패: " + e.getMessage(), e);
        }
    }

    private List<String> parseQuestions(List<String> response) {
        return response.stream()
                .map(line -> line.replaceAll("^\\d+[.)]?\\s*", "").trim()) // 숫자 제거
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}

