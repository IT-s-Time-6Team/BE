package com.team6.team6.room.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeLimitValidator implements ConstraintValidator<ValidTimeLimit, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime timeLimit, ConstraintValidatorContext context) {
        // null은 허용 (기본값 사용)
        if (timeLimit == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxTime = now.plusHours(6);
        
        // 최대 6시간 체크
        if (timeLimit.isAfter(maxTime)) {
            return false;
        }
        
        // 기준 시간과의 차이 계산 (분 단위)
        long minutesDiff = ChronoUnit.MINUTES.between(now, timeLimit);
        
        // 5분 또는 10분은 허용
        if (minutesDiff == 5 || minutesDiff == 10) {
            return true;
        }
        
        // 10분 초과 1시간 미만은 10분 단위여야 함
        if (minutesDiff > 10 && minutesDiff < 60) {
            return minutesDiff % 10 == 0;
        }
        
        // 1시간 이상은 30분 단위여야 함
        return minutesDiff % 30 == 0;
    }
}