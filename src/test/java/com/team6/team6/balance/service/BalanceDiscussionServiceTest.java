package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.entity.BalanceGameStep;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import com.team6.team6.common.timer.service.GameTimerService;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceDiscussionService 테스트")
class BalanceDiscussionServiceTest {

    @Mock
    private GameTimerService gameTimerService;

    @Mock
    private BalanceMessagePublisher messagePublisher;

    @Mock
    private BalanceVoteService balanceVoteService;

    @Mock
    private BalanceSessionService balanceSessionService;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BalanceDiscussionService balanceDiscussionService;

    @Test
    @DisplayName("토론 시간 시작 - 정상 케이스")
    void startDiscussionTime_Success_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long discussionTimeSeconds = 300L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();
        
        // discussionTimeSeconds 필드 설정
        ReflectionTestUtils.setField(balanceDiscussionService, "discussionTimeSeconds", discussionTimeSeconds);

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceDiscussionService.startDiscussionTime(roomKey, roomId);

        // then
        assertThat(session.getCurrentStep()).isEqualTo(BalanceGameStep.DISCUSSION);
        verify(gameTimerService).startTimer(any(TimerConfig.class));
    }

    @Test
    @DisplayName("토론 시간 시작 - 잘못된 단계에서 시작 시 예외")
    void startDiscussionTime_InvalidPhase_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        // WAITING_FOR_MEMBERS 단계에서 시작 시도

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when & then
        assertThatThrownBy(() -> balanceDiscussionService.startDiscussionTime(roomKey, roomId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("토론 건너뛰기 - 방장 권한 정상 케이스")
    void skipDiscussion_LeaderSuccess_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "leader";
        
        Member leaderMember = Member.builder()
                .nickname(memberName)
                .isLeader(true)
                .build();

        when(memberRepository.findByNicknameAndRoomId(memberName, roomId))
                .thenReturn(Optional.of(leaderMember));

        // when
        balanceDiscussionService.skipDiscussion(roomKey, roomId, memberName);

        // then
        verify(gameTimerService).stopTimer(anyString());
        verify(messagePublisher).notifyBalanceDiscussionSkipped(roomKey);
        verify(balanceVoteService).startVotingPhase(roomKey, roomId);
    }

    @Test
    @DisplayName("토론 건너뛰기 - 방장이 아닌 사용자 시도 시 예외")
    void skipDiscussion_NotLeader_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "normalUser";
        
        Member normalMember = Member.builder()
                .nickname(memberName)
                .isLeader(false)
                .build();

        when(memberRepository.findByNicknameAndRoomId(memberName, roomId))
                .thenReturn(Optional.of(normalMember));

        // when & then
        assertThatThrownBy(() -> balanceDiscussionService.skipDiscussion(roomKey, roomId, memberName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("방장만 토론을 건너뛸 수 있습니다");

        verify(gameTimerService, never()).stopTimer(anyString());
        verify(messagePublisher, never()).notifyBalanceDiscussionSkipped(anyString());
        verify(balanceVoteService, never()).startVotingPhase(anyString(), anyLong());
    }

    @Test
    @DisplayName("토론 건너뛰기 - 존재하지 않는 멤버 시도 시 예외")
    void skipDiscussion_MemberNotFound_Test() {
        // given
        String roomKey = "ROOM123";
        Long roomId = 1L;
        String memberName = "nonexistent";

        when(memberRepository.findByNicknameAndRoomId(memberName, roomId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> balanceDiscussionService.skipDiscussion(roomKey, roomId, memberName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멤버를 찾을 수 없습니다: " + memberName);
    }

    @Test
    @DisplayName("타이머 시작 이벤트 처리 - 밸런스 토론 타이머")
    void onTimerStart_BalanceDiscussionTimer_Test() {
        // given
        String timerKey = "balance:discussion:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long initialSeconds = 300L;
        String formattedTime = "05:00";
        TimerStartEvent event = new TimerStartEvent(timerKey, roomKey, roomId, initialSeconds, formattedTime, "BALANCE_DISCUSSION");

        // when
        balanceDiscussionService.onTimerStart(event);

        // then
        verify(messagePublisher).notifyBalanceDiscussionStarted(roomKey, formattedTime);
    }

    @Test
    @DisplayName("타이머 시작 이벤트 처리 - 다른 타이머 타입 (무시)")
    void onTimerStart_OtherTimerType_Test() {
        // given
        String timerKey = "other:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long initialSeconds = 300L;
        String formattedTime = "05:00";
        TimerStartEvent event = new TimerStartEvent(timerKey, roomKey, roomId, initialSeconds, formattedTime, "OTHER_TIMER");

        // when
        balanceDiscussionService.onTimerStart(event);

        // then
        verify(messagePublisher, never()).notifyBalanceDiscussionStarted(anyString(), anyString());
    }

    @Test
    @DisplayName("타이머 틱 이벤트 처리 - 밸런스 토론 타이머")
    void onTimerTick_BalanceDiscussionTimer_Test() {
        // given
        String timerKey = "balance:discussion:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long remainingSeconds = 270L;
        String formattedTime = "04:30";
        TimerTickEvent event = new TimerTickEvent(timerKey, roomKey, roomId, remainingSeconds, formattedTime, "BALANCE_DISCUSSION");

        // when
        balanceDiscussionService.onTimerTick(event);

        // then
        verify(messagePublisher).notifyBalanceDiscussionTimeRemaining(roomKey, formattedTime);
    }

    @Test
    @DisplayName("타이머 틱 이벤트 처리 - 다른 타이머 타입 (무시)")
    void onTimerTick_OtherTimerType_Test() {
        // given
        String timerKey = "other:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        long remainingSeconds = 270L;
        String formattedTime = "04:30";
        TimerTickEvent event = new TimerTickEvent(timerKey, roomKey, roomId, remainingSeconds, formattedTime, "OTHER_TIMER");

        // when
        balanceDiscussionService.onTimerTick(event);

        // then
        verify(messagePublisher, never()).notifyBalanceDiscussionTimeRemaining(anyString(), anyString());
    }

    @Test
    @DisplayName("타이머 종료 이벤트 처리 - 밸런스 토론 타이머")
    void onTimerEnd_BalanceDiscussionTimer_Test() {
        // given
        String timerKey = "balance:discussion:timer:ROOM123";
        String roomKey = "ROOM123";
        Long roomId = 1L;
        TimerEndEvent event = new TimerEndEvent(timerKey, roomKey, roomId, "BALANCE_DISCUSSION");

        // when
        balanceDiscussionService.onTimerEnd(event);

        // then
        verify(messagePublisher).notifyBalanceDiscussionEnded(roomKey);
        verify(balanceVoteService).startVotingPhase(roomKey, roomId);
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
        balanceDiscussionService.onTimerEnd(event);

        // then
        verify(messagePublisher, never()).notifyBalanceDiscussionEnded(anyString());
        verify(balanceVoteService, never()).startVotingPhase(anyString(), anyLong());
    }

    @Test
    @DisplayName("토론 타이머 강제 중지")
    void stopDiscussionTimer_Test() {
        // given
        String roomKey = "ROOM123";
        String expectedTimerKey = "balance:discussion:timer:" + roomKey;

        // when
        balanceDiscussionService.stopDiscussionTimer(roomKey);

        // then
        verify(gameTimerService).stopTimer(expectedTimerKey);
    }

    @Test
    @DisplayName("토론 시간 설정 확인")
    void discussionTimeConfiguration_Test() {
        // given
        long customDiscussionTime = 600L;
        ReflectionTestUtils.setField(balanceDiscussionService, "discussionTimeSeconds", customDiscussionTime);
        
        String roomKey = "ROOM123";
        Long roomId = 1L;
        
        BalanceSession session = BalanceSession.createInitialSession(roomId, 3, 3);
        session.startQuestionRevealPhase();

        when(balanceSessionService.findSessionByRoomIdWithLock(roomId)).thenReturn(session);

        // when
        balanceDiscussionService.startDiscussionTime(roomKey, roomId);

        // then
        verify(gameTimerService).startTimer(argThat(config -> 
            config.getDurationSeconds() == customDiscussionTime &&
            config.getTimerType().equals("BALANCE_DISCUSSION") &&
            config.getRoomKey().equals(roomKey) &&
            config.getRoomId().equals(roomId)
        ));
    }
} 