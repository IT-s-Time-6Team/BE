package com.team6.team6.keyword.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class KeywordExceptionHandler {

    private ResponseEntity<String> getResponseWithStatus(final HttpStatus httpStatus,
                                                         final RuntimeException exception) {
        return ResponseEntity.status(httpStatus)
                .body(exception.getMessage());
    }
}
