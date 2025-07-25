package com.team6.team6.global.error.exhandler.advice;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.global.error.exception.ExternalApiException;
import com.team6.team6.global.error.exception.NotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Object> handleNotFoundException(NotFoundException e) {
        log.error("리소스를 찾을 수 없습니다", e);
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

        log.error("잘못된 파라미터 바인딩", e);

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

        log.error("유효성 검증 실패", e);

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                errorMessages.isEmpty() ? List.of("유효하지 않은 요청입니다") : errorMessages
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ApiResponse<Object> handleIllegalStateException(Exception e) {
        log.error("잘못된 요청", e);

        List<String> errorMessages = List.of(e.getMessage());

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 요청",
                errorMessages
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error("요청 본문을 읽을 수 없습니다", e);

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                List.of("요청 본문이 필요합니다")
        );
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(ExternalApiException.class)
    public ApiResponse<Object> handleExternalApiException(ExternalApiException e) {
        log.error("외부 API 호출 실패", e);

        return ApiResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE,
                "외부 서비스 일시적 오류",
                List.of("잠시 후 다시 시도해주세요")
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Object> handleConstraintViolationException(ConstraintViolationException e) {
        List<String> errorMessages = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        log.error("요청 파라미터 검증 실패", e);

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                errorMessages.isEmpty() ? List.of("유효하지 않은 요청입니다") : errorMessages
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Object> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("필수 파라미터 누락", e);

        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                "잘못된 파라미터",
                List.of(e.getParameterName() + " 파라미터가 필요합니다")
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
