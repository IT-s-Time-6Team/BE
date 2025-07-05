package com.team6.team6.tmi.service;

import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.entity.TmiSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TmiHintService {

    private static final String HINT_TIMER_KEY = "tmi:hint:timer:";

    @Value("${tmi.hint.time.seconds}")
    private long hintTimeSeconds;

    private final StringRedisTemplate redisTemplate;
    private final TmiMessagePublisher messagePublisher;
    private final TmiVoteService tmiVoteService;
    private final TmiSessionService tmiSessionService;

    @Transactional
    public void startHintTime(String roomKey, Long roomId) {
        TmiSession session = tmiSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증
        session.validateCanStartHint();

        // 세션을 힌트 단계로 변경
        session.startHintTime();

        log.debug("TMI 힌트 타임 시작: roomKey={}", roomKey);

        // 힌트 타이머 시작
        startHintTimer(roomKey, roomId);
    }

    private void startHintTimer(String roomKey, Long roomId) {
        String timerKey = HINT_TIMER_KEY + roomKey;
        redisTemplate.opsForValue().set(timerKey, String.valueOf(hintTimeSeconds));

        // 타이머 시작 알림
        String formattedTime = formatTime(hintTimeSeconds);
        messagePublisher.notifyTmiHintStarted(roomKey, formattedTime);

        // 스케줄러 생성 및 실행
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            Long remainingSeconds = Optional.ofNullable(redisTemplate.opsForValue().get(timerKey))
                    .map(Long::parseLong)
                    .orElse(0L);

            if (remainingSeconds <= 0) {
                // 타이머 종료
                redisTemplate.delete(timerKey);
                scheduler.shutdown();
                messagePublisher.notifyTmiHintEnded(roomKey);
                tmiVoteService.startVotingPhase(roomKey, roomId);
                return;
            }

            // 남은 시간 감소
            remainingSeconds--;
            redisTemplate.opsForValue().set(timerKey, String.valueOf(remainingSeconds));

            // 남은 시간 브로드캐스팅
            String formattedRemaining = formatTime(remainingSeconds);
            messagePublisher.notifyTmiHintTimeRemaining(roomKey, formattedRemaining);

        }, 0, 1, TimeUnit.SECONDS);
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
