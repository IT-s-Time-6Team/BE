package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.common.timer.dto.TimerConfig;
import com.team6.team6.common.timer.event.TimerEndEvent;
import com.team6.team6.common.timer.event.TimerStartEvent;
import com.team6.team6.common.timer.event.TimerTickEvent;
import com.team6.team6.common.timer.service.GameTimerService;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceDiscussionService {

    private static final String DISCUSSION_TIMER_KEY_PREFIX = "balance:discussion:timer:";
    private static final String DISCUSSION_TIMER_TYPE = "BALANCE_DISCUSSION";

    @Value("${balance.discussion.time.seconds:300}")
    private long discussionTimeSeconds;

    private final GameTimerService gameTimerService;
    private final BalanceMessagePublisher messagePublisher;
    private final BalanceVoteService balanceVoteService;
    private final BalanceSessionService balanceSessionService;
    private final MemberRepository memberRepository;

    @Transactional
    public void startDiscussionTime(String roomKey, Long roomId) {
        BalanceSession session = balanceSessionService.findSessionByRoomIdWithLock(roomId);

        // 상태 검증 및 토론 단계로 변경
        session.startDiscussionPhase();

        log.debug("밸런스 토론 시작: roomKey={}, discussionTimeSeconds={}", roomKey, discussionTimeSeconds);

        // 공통 타이머 서비스를 통해 토론 타이머 시작
        String timerKey = DISCUSSION_TIMER_KEY_PREFIX + roomKey;
        TimerConfig config = TimerConfig.of(timerKey, roomKey, roomId, discussionTimeSeconds, DISCUSSION_TIMER_TYPE);
        gameTimerService.startTimer(config);
    }

    @Transactional
    public void skipDiscussion(String roomKey, Long roomId, String memberName) {
        // 방장 권한 확인
        validateLeaderPermission(roomId, memberName);

        // 토론 타이머 강제 중지
        stopDiscussionTimer(roomKey);

        log.debug("밸런스 토론 건너뛰기: roomKey={}, leader={}", roomKey, memberName);

        // 건너뛰기 메시지 발행
        messagePublisher.notifyBalanceDiscussionSkipped(roomKey);

        // 투표 단계 시작
        balanceVoteService.startVotingPhase(roomKey, roomId);
    }

    /**
     * 방장 권한 확인
     */
    private void validateLeaderPermission(Long roomId, String memberName) {
        Member member = memberRepository.findByNicknameAndRoomId(memberName, roomId)
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberName));
        
        if (!member.isLeader()) {
            throw new IllegalStateException("방장만 토론을 건너뛸 수 있습니다");
        }
    }

    /**
     * 토론 타이머 시작 이벤트 처리
     */
    @EventListener
    public void onTimerStart(TimerStartEvent event) {
        if (!DISCUSSION_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 토론 타이머 시작 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 타이머 시작 메시지 발행
        messagePublisher.notifyBalanceDiscussionStarted(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 토론 타이머 틱 이벤트 처리 (매초마다)
     */
    @EventListener
    public void onTimerTick(TimerTickEvent event) {
        if (!DISCUSSION_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 토론 타이머 틱 이벤트 수신: roomKey={}, remainingTime={}",
                event.getRoomKey(), event.getFormattedTime());

        // 남은 시간 브로드캐스팅
        messagePublisher.notifyBalanceDiscussionTimeRemaining(event.getRoomKey(), event.getFormattedTime());
    }

    /**
     * 토론 타이머 종료 이벤트 처리
     */
    @EventListener
    public void onTimerEnd(TimerEndEvent event) {
        if (!DISCUSSION_TIMER_TYPE.equals(event.getTimerType())) {
            return;
        }

        log.debug("밸런스 토론 타이머 종료 이벤트 수신: roomKey={}", event.getRoomKey());

        // 토론 종료 메시지 발행
        messagePublisher.notifyBalanceDiscussionEnded(event.getRoomKey());

        // 투표 단계 시작
        balanceVoteService.startVotingPhase(event.getRoomKey(), event.getRoomId());
    }

    /**
     * 토론 타이머 강제 중지
     */
    public void stopDiscussionTimer(String roomKey) {
        String timerKey = DISCUSSION_TIMER_KEY_PREFIX + roomKey;
        gameTimerService.stopTimer(timerKey);
        log.debug("밸런스 토론 타이머 강제 중지: roomKey={}", roomKey);
    }
} 