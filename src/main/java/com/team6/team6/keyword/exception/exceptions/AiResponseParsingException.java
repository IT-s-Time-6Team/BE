package com.team6.team6.keyword.exception.exceptions;

public class AiResponseParsingException extends RuntimeException {

    public AiResponseParsingException(Throwable cause) {
        super("AI 응답을 파싱하는 데 실패했습니다.", cause);
    }
}
