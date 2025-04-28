package com.team6.team6.keyword.exception.exceptions;

public class InvalidAiResponseException extends RuntimeException {

    public InvalidAiResponseException() {
        super("AI로부터 유효한 응답을 받지 못했습니다.");
    }
}
