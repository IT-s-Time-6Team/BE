package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.entity.BalanceGameStep;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import com.team6.team6.common.timer.service.GameTimerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceRevealService 테스트")
class BalanceRevealServiceTest {

    @Mock
    private GameTimerService gameTimerService;

    @Mock
    private BalanceMessagePublisher messagePublisher;

    @Mock
    private BalanceDiscussionService balanceDiscussionService;

    @Mock
    private BalanceSessionService balanceSessionService;

    @InjectMocks
    private BalanceRevealService balanceRevealService;

    @Test
    @DisplayName("문제 공개 시작 - 정상 케이스")
    void startQuestionReveal_Success_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long revealTimeSeconds = 30L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        
        // revealTimeSeconds 필드 설정
        ReflectionTestUtils.setField(balanceRevealService, "revealTimeSeconds", revealTimeSeconds);

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceRevealService.startQuestionReveal(roomKey, roomId);

        // then
        verify(gameTimerService).startTimer(any(TimerConfig.class));
    }

    @Test
    @DisplayName("문제 공개 시작 - 잘못된 단계에서 시작 시 예외")
    void startQuestionReveal_InvalidPhase_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        // WAITING_FOR_MEMBERS 단계에서 시작 시도

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceRevealService.startQuestionReveal(roomKey, roomId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("타이머 시작 이벤트 처리 - 밸런스 공개 타이머")
    void onTimerStart_BalanceRevealTimer_Test() {
        // given
        String timerKey = "balance:reveal:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long initialSeconds = 30L;
        String formattedTime = "00:30";
        TimerStartEvent event = new TimerStartEvent(timerKey, roomKey, roomId, initialSeconds, formattedTime, "BALANCE_REVEAL");

        // when
        balanceRevealService.onTimerStart(event);

        // then
        verify(messagePublisher).notifyBalanceQuestionStarted(roomKey, formattedTime);
    }

    @Test
    @DisplayName("타이머 시작 이벤트 처리 - 다른 타이머 타입 (무시)")
    void onTimerStart_OtherTimerType_Test() {
        // given
        String timerKey = "other:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long initialSeconds = 30L;
        String formattedTime = "00:30";
        TimerStartEvent event = new TimerStartEvent(timerKey, roomKey, roomId, initialSeconds, formattedTime, "OTHER_TIMER");

        // when
        balanceRevealService.onTimerStart(event);

        // then
        verify(messagePublisher, never()).notifyBalanceQuestionStarted(anyString(), anyString());
    }

    @Test
    @DisplayName("타이머 틱 이벤트 처리 - 밸런스 공개 타이머")
    void onTimerTick_BalanceRevealTimer_Test() {
        // given
        String timerKey = "balance:reveal:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long remainingSeconds = 15L;
        String formattedTime = "00:15";
        TimerTickEvent event = new TimerTickEvent(timerKey, roomKey, roomId, remainingSeconds, formattedTime, "BALANCE_REVEAL");

        // when
        balanceRevealService.onTimerTick(event);

        // then
        verify(messagePublisher).notifyBalanceQuestionTimeRemaining(roomKey, formattedTime);
    }

    @Test
    @DisplayName("타이머 틱 이벤트 처리 - 다른 타이머 타입 (무시)")
    void onTimerTick_OtherTimerType_Test() {
        // given
        String timerKey = "other:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long remainingSeconds = 15L;
        String formattedTime = "00:15";
        TimerTickEvent event = new TimerTickEvent(timerKey, roomKey, roomId, remainingSeconds, formattedTime, "OTHER_TIMER");

        // when
        balanceRevealService.onTimerTick(event);

        // then
        verify(messagePublisher, never()).notifyBalanceQuestionTimeRemaining(anyString(), anyString());
    }

    @Test
    @DisplayName("타이머 종료 이벤트 처리 - 밸런스 공개 타이머")
    void onTimerEnd_BalanceRevealTimer_Test() {
        // given
        String timerKey = "balance:reveal:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        TimerEndEvent event = new TimerEndEvent(timerKey, roomKey, roomId, "BALANCE_REVEAL");

        // when
        balanceRevealService.onTimerEnd(event);

        // then
        verify(messagePublisher).notifyBalanceQuestionEnded(roomKey);
        verify(balanceDiscussionService).startDiscussionTime(roomKey, roomId);
    }

    @Test
    @DisplayName("타이머 종료 이벤트 처리 - 다른 타이머 타입 (무시)")
    void onTimerEnd_OtherTimerType_Test() {
        // given
        String timerKey = "other:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        TimerEndEvent event = new TimerEndEvent(timerKey, roomKey, roomId, "OTHER_TIMER");

        // when
        balanceRevealService.onTimerEnd(event);

        // then
        verify(messagePublisher, never()).notifyBalanceQuestionEnded(anyString());
        verify(balanceDiscussionService, never()).startDiscussionTime(anyString(), anyLong());
    }

    @Test
    @DisplayName("문제 공개 타이머 강제 중지")
    void stopRevealTimer_Test() {
        // given
        String roomKey = "ROOM123";
        String expectedTimerKey = "balance:reveal:timer:" + roomKey;

        // when
        balanceRevealService.stopRevealTimer(roomKey);

        // then
        verify(gameTimerService).stopTimer(expectedTimerKey);
    }

    @Test
    @DisplayName("공개 시간 설정 확인")
    void revealTimeConfiguration_Test() {
        // given
        long customRevealTime = 60L;
        ReflectionTestUtils.setField(balanceRevealService, "revealTimeSeconds", customRevealTime);
        
        String roomKey = "ROOM123";
        Long roomId = 1L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceRevealService.startQuestionReveal(roomKey, roomId);

        // then
        verify(gameTimerService).startTimer(argThat(config -> 
            config.getDurationSeconds() == customRevealTime &&
            config.getTimerType().equals("BALANCE_REVEAL") &&
            config.getRoomKey().equals(roomKey) &&
            config.getRoomId().equals(roomId)
        ));
    }

    @Test
    @DisplayName("문제 공개 세션 상태 확인")
    void startQuestionReveal_SessionStateCheck_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        
        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceRevealService.startQuestionReveal(roomKey, roomId);

        // then
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.QUESTION_REVEAL);
        verify(balanceSessionService).findSessionByRoomIdWithLock(roomId);
    }
} 