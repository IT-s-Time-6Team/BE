package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import com.team6.team6.common.timer.service.GameTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceRevealService {

    private static final String REVEAL_TIMER_KEY_PREFIX = "balance:reveal:timer:";
    private static final String REVEAL_TIMER_TYPE = "BALANCE_REVEAL";

    @Value("${balance.reveal.time.seconds:30}")
    private long revealTimeSeconds;

    private final GameTimerService gameTimerService;
    private final BalanceMessagePublisher messagePublisher;
    private final BalanceDiscussionService balanceDiscussionService;
    private final BalanceSessionService balanceSessionService;

    @Transactional
    public void startQuestionReveal(String roomKey, Long roomId) {
        BalanceSession session = balanceSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증
        session.requireQuestionRevealPhase();

        log.debug("밸런스 문제 공개 시작: roomKey={}, revealTimeSeconds={}", roomKey, revealTimeSeconds);

        // 공통 타이머 서비스를 통해 문제 공개 타이머 시작
        String timerKey = REVEAL_TIMER_KEY_PREFIX + roomKey;
        TimerConfig config = TimerConfig.of(timerKey, roomKey, roomId, revealTimeSeconds, REVEAL_TIMER_TYPE);
        gameTimerService.startTimer(config);
    }

    /**
     * 문제 공개 타이머 시작 이벤트 처리
     */
    @EventListener
    public void onTimerStart(TimerStartEvent event) {
        if (!REVEAL_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 문제 공개 타이머 시작 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 타이머 시작 메시지 발행
        messagePublisher.notifyBalanceQuestionStarted(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 문제 공개 타이머 틱 이벤트 처리 (매초마다)
     */
    @EventListener
    public void onTimerTick(TimerTickEvent event) {
        if (!REVEAL_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 문제 공개 타이머 틱 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 남은 시간 브로드캐스팅
        messagePublisher.notifyBalanceQuestionTimeRemaining(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 문제 공개 타이머 종료 이벤트 처리
     */
    @EventListener
    public void onTimerEnd(TimerEndEvent event) {
        if (!REVEAL_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 문제 공개 타이머 종료 이벤트 수신: roomKey={}", event.getRoomKey());

        // 문제 공개 종료 메시지 발행
        messagePublisher.notifyBalanceQuestionEnded(event.getRoomKey());

        // 토론 단계 시작
        balanceDiscussionService.startDiscussionTime(event.getRoomKey(), event.getRoomId());
    }

    /**
     * 문제 공개 타이머 강제 중지
     */
    public void stopRevealTimer(String roomKey) {
        String timerKey = REVEAL_TIMER_KEY_PREFIX + roomKey;
        gameTimerService.stopTimer(timerKey);
        log.debug("밸런스 문제 공개 타이머 강제 중지: roomKey={}", roomKey);
    }
} 