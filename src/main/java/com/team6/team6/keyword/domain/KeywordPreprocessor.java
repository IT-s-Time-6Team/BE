package com.team6.team6.keyword.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KeywordPreprocessor {
    /**
     * 키워드 전처리: 대소문자 무시, 공백 제거, 특수문자 제거
     *
     * @param keyword 입력 키워드
     * @return 전처리된 키워드
     */
    public String preprocess(String keyword) {
        if (keyword == null) return null;
        // 1. 소문자로 변환
        String processed = keyword.toLowerCase();
        // 2. 공백 제거
        processed = processed.replaceAll("\\s+", "");
        // 3. 특수문자 제거 (영문, 숫자, 한글만 남김)
        processed = processed.replaceAll("[^a-z0-9가-힣]", "");
        log.info("{} 전처리 결과: {}", keyword, processed);
        return processed;
    }
}

