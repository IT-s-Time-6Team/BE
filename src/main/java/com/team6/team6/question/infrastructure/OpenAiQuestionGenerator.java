package com.team6.team6.question.infrastructure;

import com.team6.team6.question.domain.QuestionGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiQuestionGenerator implements QuestionGenerator {

    private final ChatClient chatClient;

    private static final PromptTemplate PROMPT_TEMPLATE = new PromptTemplate("""
                키워드: {keyword}

                이 키워드를 주제로 한, 처음 만난 사람들이 편하게 이야기 나눌 수 있도록 가벼운 질문 20개를 만들어줘.
                형식은 번호 없이 한 줄에 하나씩, 자연스럽게 구어체로 작성해줘.
            """
    );

    @Override
    public List<String> generateQuestions(String keyword) {
        Prompt prompt = PROMPT_TEMPLATE.create(Map.of("keyword", keyword));

        try {
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();

            return parseQuestions(response);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 질문 생성 실패: " + e.getMessage(), e);
        }
    }

    private List<String> parseQuestions(String response) {
        return Arrays.stream(response.split("\n"))
                .map(line -> line.replaceAll("^\\d+[.)]?\\s*", "").trim()) // 숫자 제거
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}

