package com.team6.team6.keyword.exception.exceptions;

public class EmptyKeywordException extends RuntimeException {

    public EmptyKeywordException() {
        super("분석할 키워드가 없습니다.");
    }
}
