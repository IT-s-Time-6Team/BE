package com.team6.team6.keyword.exception;

import com.team6.team6.keyword.exception.exceptions.AiResponseParsingException;
import com.team6.team6.keyword.exception.exceptions.EmptyKeywordException;
import com.team6.team6.keyword.exception.exceptions.InvalidAiResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class KeywordExceptionHandler {

    @ExceptionHandler(EmptyKeywordException.class)
    public ResponseEntity<String> handleEmptyKeywordException(final EmptyKeywordException exception) {
        return getResponseWithStatus(HttpStatus.BAD_REQUEST, exception);
    }

    @ExceptionHandler(AiResponseParsingException.class)
    public ResponseEntity<String> handleAiResponseParsingException(final AiResponseParsingException exception) {
        return getResponseWithStatus(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler(InvalidAiResponseException.class)
    public ResponseEntity<String> handleInvalidAiResponseException(final InvalidAiResponseException exception) {
        return getResponseWithStatus(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private ResponseEntity<String> getResponseWithStatus(final HttpStatus httpStatus,
                                                         final RuntimeException exception) {
        return ResponseEntity.status(httpStatus)
                .body(exception.getMessage());
    }
}
