package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.KeywordSimilarityAnalyser;
import com.team6.team6.keyword.dto.KeywordGroupResponse;
import com.team6.team6.keyword.exception.AiResponseParsingException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.team6.team6.global.log.LogUtil.infoLog;

@Component
@RequiredArgsConstructor
public class OpenAiKeywordSimilarityAnalyser implements KeywordSimilarityAnalyser {

    private final ChatClient chatClient;

    @Override
    public List<List<String>> analyse(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            infoLog("키워드 유사성 분석 요청이 비어있어 빈 결과 반환");
            return List.of();
        }

        infoLog(String.format("분석 요청 키워드 목록: %s", String.join(", ", keywords)));

        String formattedKeywords = keywords.stream()
                .map(k -> "- " + k)
                .collect(Collectors.joining("\n"));

        String promptText = String.format("""
                    다음은 여러 사용자가 입력한 관심사 목록입니다. 의미상 같은 것을 그룹으로 묶어주세요.
                
                    분석 기준:
                    - 약어, 줄임말, 은어, 다국어 표현, 맞춤법 오류 등을 고려
                    - 예: '롤', 'lol', '리그오브레전드'는 같은 게임으로 묶기
                
                    입력:
                    %s
                
                    출력 형식은 JSON 배열입니다. 설명 없이 JSON 배열만 출력하세요.
                    입력된 순서를 지켜서 그룹화하세요.
                """, formattedKeywords);

        Prompt prompt = new Prompt(promptText);

        try {
            infoLog(String.format("OpenAI API 호출 시작 - 키워드 개수: %d", keywords.size()));
            KeywordGroupResponse response = chatClient
                    .prompt(prompt)
                    .call()
                    .entity(KeywordGroupResponse.class);
            List<List<String>> groups = response.groups();

            return response.groups();
        } catch (Exception e) {
            throw new AiResponseParsingException(e);
            infoLog(String.format("OpenAI로부터 키워드 유사성 분석 완료 - 그룹 수: %d, 입력 키워드 수: %d",
                    groups.size(), keywords.size()));

            return groups;
        }
    }
}
