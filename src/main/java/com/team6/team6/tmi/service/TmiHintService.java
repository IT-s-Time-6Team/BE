package com.team6.team6.tmi.service;

import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import com.team6.team6.common.timer.service.GameTimerService;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import com.team6.team6.tmi.domain.TmiMessagePublisher;
import com.team6.team6.tmi.entity.TmiSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class TmiHintService {

    private static final String HINT_TIMER_KEY_PREFIX = "tmi:hint:timer:";
    private static final String HINT_TIMER_TYPE = "TMI_HINT";

    @Value("${tmi.hint.time.seconds:300}")
    private long hintTimeSeconds;

    private final GameTimerService gameTimerService;
    private final TmiMessagePublisher messagePublisher;
    private final TmiVoteService tmiVoteService;
    private final TmiSessionService tmiSessionService;
    private final MemberRepository memberRepository;

    @Transactional
    public void startHintTime(String roomKey, Long roomId) {
        TmiSession session = tmiSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증
        session.validateCanStartHint();

        // 세션을 힌트 단계로 변경
        session.startHintTime();

        log.debug("TMI 힌트 타임 시작: roomKey={}, hintTimeSeconds={}", roomKey, hintTimeSeconds);

        // 공통 타이머 서비스를 통해 힌트 타이머 시작
        String timerKey = HINT_TIMER_KEY_PREFIX + roomKey;
        TimerConfig config = TimerConfig.of(timerKey, roomKey, roomId, hintTimeSeconds, HINT_TIMER_TYPE);
        gameTimerService.startTimer(config);
    }

    /**
     * 힌트 타이머 시작 이벤트 처리
     */
    @EventListener
    public void onTimerStart(TimerStartEvent event) {
        if (!HINT_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("TMI 힌트 타이머 시작 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 타이머 시작 메시지 발행
        messagePublisher.notifyTmiHintStarted(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 힌트 타이머 틱 이벤트 처리 (매초마다)
     */
    @EventListener
    public void onTimerTick(TimerTickEvent event) {
        if (!HINT_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("TMI 힌트 타이머 틱 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 남은 시간 브로드캐스팅
        messagePublisher.notifyTmiHintTimeRemaining(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 힌트 타이머 종료 이벤트 처리
     */
    @EventListener
    public void onTimerEnd(TimerEndEvent event) {
        if (!HINT_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("TMI 힌트 타이머 종료 이벤트 수신: roomKey={}", event.getRoomKey());

        // 힌트 종료 메시지 발행
        messagePublisher.notifyTmiHintEnded(event.getRoomKey());

        // 투표 단계 시작
        tmiVoteService.startVotingPhase(event.getRoomKey(), event.getRoomId());
    }

    /**
     * 힌트 타이머 강제 중지
     */
    public void stopHintTimer(String roomKey) {
        String timerKey = HINT_TIMER_KEY_PREFIX + roomKey;
        gameTimerService.stopTimer(timerKey);
        log.debug("TMI 힌트 타이머 강제 중지: roomKey={}", roomKey);
    }

    /**
     * 힌트 타이머 건너뛰기 (방장 전용)
     */
    @Transactional
    public void skipHintTime(String roomKey, Long roomId, String memberName) {
        // 방장 권한 확인
        validateLeaderPermission(roomId, memberName);

        // 타이머 중지
        stopHintTimer(roomKey);

        log.debug("TMI 힌트 타임 건너뛰기: roomKey={}, leader={}", roomKey, memberName);

        // 건너뛰기 메시지 발행
        messagePublisher.notifyTmiHintSkipped(roomKey);

        // 투표 단계 시작
        tmiVoteService.startVotingPhase(roomKey, roomId);
    }

    /**
     * 방장 권한 확인
     */
    private void validateLeaderPermission(Long roomId, String memberName) {
        Member member = memberRepository.findByNicknameAndRoomId(memberName, roomId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberName));

        if (!member.isLeader()) {
            throw new IllegalStateException("방장만 힌트 타임을 건너뛸 수 있습니다");
        }
    }
}
