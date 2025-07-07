package com.team6.team6.common.timer.service;

import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameTimerService {

    private final StringRedisTemplate redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 타이머 시작
     */
    public void startTimer(TimerConfig config) {
        String timerKey = config.getTimerKey();

        // Redis에 초기 시간 설정
        redisTemplate.opsForValue().set(timerKey, String.valueOf(config.getDurationSeconds()));

        // 타이머 시작 이벤트 발행
        String formattedTime = formatTime(config.getDurationSeconds());
        eventPublisher.publishEvent(new TimerStartEvent(
                timerKey, config.getRoomKey(), config.getRoomId(),
                config.getDurationSeconds(), formattedTime, config.getTimerType()));

        log.debug("타이머 시작: timerKey={}, type={}, duration={}초",
                timerKey, config.getTimerType(), config.getDurationSeconds());

        // 스케줄러 생성 및 실행
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            // Redis에서 남은 시간 조회
            Long remainingSeconds = Optional.ofNullable(redisTemplate.opsForValue().get(timerKey))
                    .map(Long::parseLong)
                    .orElse(0L);

            if (remainingSeconds <= 0) {
                // 타이머 종료
                redisTemplate.delete(timerKey);
                scheduler.shutdown();

                // 타이머 종료 이벤트 발행
                eventPublisher.publishEvent(new TimerEndEvent(
                        timerKey, config.getRoomKey(), config.getRoomId(), config.getTimerType()));

                log.debug("타이머 종료: timerKey={}, type={}", timerKey, config.getTimerType());
                return;
            }

            // 남은 시간 감소
            remainingSeconds--;
            redisTemplate.opsForValue().set(timerKey, String.valueOf(remainingSeconds));

            // 타이머 틱 이벤트 발행
            String formattedRemaining = formatTime(remainingSeconds);
            eventPublisher.publishEvent(new TimerTickEvent(
                    timerKey, config.getRoomKey(), config.getRoomId(),
                    remainingSeconds, formattedRemaining, config.getTimerType()));

        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 타이머 중지 (강제 종료)
     */
    public void stopTimer(String timerKey) {
        if (redisTemplate.hasKey(timerKey)) {
            redisTemplate.delete(timerKey);
            log.debug("타이머 강제 중지: timerKey={}", timerKey);
        }
    }

    /**
     * 시간 포맷팅
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * 남은 시간 조회
     */
    public long getRemainingSeconds(String timerKey) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(timerKey))
                .map(Long::parseLong)
                .orElse(0L);
    }

    /**
     * 타이머 실행 중인지 확인
     */
    public boolean isTimerRunning(String timerKey) {
        return redisTemplate.hasKey(timerKey);
    }
} 