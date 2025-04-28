package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.KeywordSimilarityAnalyser;
import com.team6.team6.keyword.exception.exceptions.AiResponseParsingException;
import com.team6.team6.keyword.exception.exceptions.EmptyKeywordException;
import com.team6.team6.keyword.exception.exceptions.InvalidAiResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiKeywordSimilarityAnalyser implements KeywordSimilarityAnalyser {

    private final ChatClient chatClient;

    @Override
    public List<List<String>> analyse(List<String> keywords) {
        String prompt = buildPrompt(keywords);

        try {
            List<List<String>> result = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });

            if (result.isEmpty()) {
                throw new InvalidAiResponseException();
            }

            return result;
        } catch (Exception e) {
            throw new AiResponseParsingException(e);
        }
    }

    private String buildPrompt(List<String> keywords) {

        if (keywords == null || keywords.isEmpty()) {
            throw new EmptyKeywordException();
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("다음은 여러 사용자가 입력한 관심사 목록입니다. 의미상 같은 것을 그룹으로 묶어주세요.\n\n")
                .append("분석 기준:\n")
                .append("- 약어, 줄임말, 은어, 다국어 표현, 맞춤법 오류 등을 고려\n")
                .append("- 예: '롤', 'lol', '리그오브레전드'는 같은 게임으로 묶기\n\n")
                .append("입력:\n");

        for (String keyword : keywords) {
            prompt.append("- ").append(keyword).append("\n");
        }

        prompt.append("출력 형식은 JSON 배열입니다. 설명 없이 JSON 배열만 출력하세요. 입력된 순서를 지켜서 그룹화하세요.");
        return prompt.toString();
    }
}
