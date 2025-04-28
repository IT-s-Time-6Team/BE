package com.team6.team6.global.error.exhandler.advice;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.global.error.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Object> handleNotFoundException(NotFoundException e) {
        return ApiResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                null
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> handleBindException(BindException e) {
        List<String> errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                errorMessages.isEmpty() ? List.of("유효하지 않은 요청입니다") : errorMessages
        );
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<String> errorMessages = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                errorMessages.isEmpty() ? List.of("유효하지 않은 요청입니다") : errorMessages
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Object> handleIllegalStateException(IllegalStateException e) {
        List<String> errorMessages = List.of(e.getMessage());

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                errorMessages
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Object> handleHttpMessageNotReadableException() {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                List.of("요청 본문이 필요합니다")
        );
    }

//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(Exception.class)
//    public ApiResponse<Object> handleException(Exception e) {
//        return ApiResponse.of(
//                HttpStatus.INTERNAL_SERVER_ERROR,
//                "서버 오류",
//                null
//        );
//    }


}