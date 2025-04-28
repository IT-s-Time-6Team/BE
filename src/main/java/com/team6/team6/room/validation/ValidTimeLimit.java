package com.team6.team6.room.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TimeLimitValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTimeLimit {
    String message() default "유효하지 않은 시간 제한입니다. 5분, 10분, 이후 10분 단위, 1시간 이후 30분 단위, 최대 6시간까지 설정 가능합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}