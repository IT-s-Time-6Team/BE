package com.team6.team6.keyword.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordPreprocessorTest {

    private KeywordPreprocessor keywordPreprocessor;

    @BeforeEach
    void setUp() {
        keywordPreprocessor = new KeywordPreprocessor();
    }

    @Test
    @DisplayName("null 키워드를 전처리하면 null을 반환한다")
    void preprocessNullKeyword() {
        // given
        String keyword = null;

        // when
        String result = keywordPreprocessor.preprocess(keyword);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 키워드를 전처리하면 빈 문자열을 반환한다")
    void preprocessEmptyKeyword() {
        // given
        String keyword = "";

        // when
        String result = keywordPreprocessor.preprocess(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("키워드의 대소문자 변환, 공백 제거, 특수문자 제거가 정상적으로 이루어진다")
    @CsvSource({
            "'Hello World', 'helloworld'",
            "'JAVA programming', 'javaprogramming'",
            "'Spring Boot!', 'springboot'",
            "'Hello@123', 'hello123'",
            "'안녕 세상!', '안녕세상'",
            "'한글123TEST', '한글123test'",
            "'      spaces     ', 'spaces'",
            "'!@#$%^&*()_+', ''",
            "'Mixed 한글 and English!', 'mixed한글andenglish'"
    })
    void preprocessKeyword(String input, String expected) {
        // when
        String result = keywordPreprocessor.preprocess(input);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("복잡한 텍스트 전처리 테스트")
    void preprocessComplexString() {
        // given
        String complex = "This is a TEST! 안녕하세요? 123-456-7890 #HashTag @mention";

        // when
        String result = keywordPreprocessor.preprocess(complex);

        // then
        assertThat(result).isEqualTo("thisisatest안녕하세요1234567890hashtagmention");
    }
}
