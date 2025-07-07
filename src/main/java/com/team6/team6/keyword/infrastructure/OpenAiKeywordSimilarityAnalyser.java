package com.team6.team6.keyword.infrastructure;

import com.team6.team6.global.error.exception.ExternalApiException;
import com.team6.team6.global.log.LogMarker;
import com.team6.team6.keyword.domain.KeywordSimilarityAnalyser;
import com.team6.team6.keyword.dto.KeywordGroupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiKeywordSimilarityAnalyser implements KeywordSimilarityAnalyser {

    private final ChatClient chatClient;

    @Override
    public List<List<String>> analyse(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            log.info(LogMarker.OPEN_AI.getMarker(), "키워드 유사성 분석 요청이 비어있어 빈 결과 반환");
            return List.of();
        }

        log.info(LogMarker.OPEN_AI.getMarker(),
                "분석 요청 키워드 목록: {}", String.join(", ", keywords));

        String formattedKeywords = keywords.stream()
                .map(k -> "- " + k)
                .collect(Collectors.joining("\n"));

        String promptText = String.format("""
                    다음은 여러 사용자가 입력한 관심사 목록입니다. 의미상 같은 것을 그룹으로 묶어주세요.
                
                    규칙:
                    - 약어, 줄임말, 은어, 다국어 표현, 맞춤법 오류 등을 고려해서 그룹화하세요.
                    - 오직 입력된 키워드만 사용하세요. 새로운 키워드를 추가하지 마세요.
                
                    입력:
                    %s
                
                    출력 형식은 JSON 배열입니다. 설명 없이 JSON 배열만 출력하세요.
                    입력된 순서를 지켜서 그룹화하세요.
                """, formattedKeywords);

        Prompt prompt = new Prompt(promptText);

        try {
            log.info(LogMarker.OPEN_AI.getMarker(),
                    "OpenAI API 호출 시작 - 키워드 개수: {}", keywords.size());

            KeywordGroupResponse response = chatClient
                    .prompt(prompt)
                    .call()
                    .entity(KeywordGroupResponse.class);
            List<List<String>> groups = response.groups();

            log.info(LogMarker.OPEN_AI.getMarker(),
                    "OpenAI로부터 키워드 유사성 분석 완료 - 그룹 수: {}, 입력 키워드 수: {}",
                    groups.size(), keywords.size());

            return groups;
        } catch (RuntimeException e) {
            log.error(LogMarker.OPEN_AI.getMarker(), "OpenAI 키워드 유사성 분석 실패", e);
            throw new ExternalApiException("OpenAI 키워드 유사성 분석 실패", e);
        }
    }
}
