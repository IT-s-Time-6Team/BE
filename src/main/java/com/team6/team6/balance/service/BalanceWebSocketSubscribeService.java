package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.dto.BalanceChatMessage;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Balance 모드 WebSocket 구독 관련 서비스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceWebSocketSubscribeService {

    private final RoomMemberStateManager roomMemberStateManager;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;
    private final BalanceSessionService balanceSessionService;
    private final BalanceMessagePublisher balanceMessagePublisher;
    private final BalanceRevealService balanceRevealService;
    private final BalanceScoreService balanceScoreService;

    @Transactional
    public ChatMessage handleUserSubscription(String roomKey, String nickname, Long roomId, Long memberId) {
        log.debug("Balance 사용자 구독 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        // 첫 연결인지 확인 (연결 시점에서 설정된 상태 확인)
        boolean isFirstConnection = roomMemberStateManager.isFirstConnection(roomKey, nickname);

        log.debug("연결 상태 확인: roomKey={}, nickname={}, isFirstConnection={}",
                roomKey, nickname, isFirstConnection);

        if (isFirstConnection) {
            // 첫 입장인 경우 - 멤버 입장 처리
            return handleEnter(roomKey, nickname, roomId);
        } else {
            // 재입장인 경우 - 특별한 처리 없이 null 반환
            return handleReenter(roomKey, nickname, roomId);
        }
    }

    private ChatMessage handleEnter(String roomKey, String nickname, Long roomId) {
        log.debug("Balance 첫 입장 처리 시작: roomKey={}, nickname={}, roomId={}", roomKey, nickname, roomId);

        // 방 정보 조회
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + roomKey));

        // 현재 온라인 멤버 수 조회
        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        
        log.debug("Balance 온라인 사용자 수 조회 완료: roomKey={}, onlineUserCount={}, maxMember={}",
                roomKey, onlineUserCount, room.getMaxMember());

        // 멤버 입장 알림 메시지 생성
        ChatMessage enterMessage = BalanceChatMessage.enter(nickname, onlineUserCount, room.getMaxMember());

        // 모든 멤버가 입장했는지 체크
        if (onlineUserCount >= room.getMaxMember()) {
            log.debug("모든 멤버 입장 완료: roomKey={}, onlineUserCount={}, maxMember={}",
                    roomKey, onlineUserCount, room.getMaxMember());
            
            // Balance 게임 시작 처리
            boolean gameStarted = balanceSessionService.checkMemberJoinAndStartIfReady(roomId, onlineUserCount);
            
            if (gameStarted) {
                // 멤버 점수 초기화
                initializeMemberScores(roomId);
                
                // 모든 멤버 입장 완료 알림 (비동기)
                balanceMessagePublisher.notifyBalanceAllMembersJoined(roomKey);
                
                // 문제 공개 단계 30초 타이머 시작 (비동기)
                balanceRevealService.startQuestionReveal(roomKey, roomId);
                
                log.info("Balance 게임 시작됨: roomKey={}, roomId={}", roomKey, roomId);
            }
        }

        log.debug("Balance 입장 메시지 생성 완료: nickname={}, type={}, content={}",
                nickname, enterMessage.getType(), enterMessage.getContent());

        return enterMessage;
    }

    private ChatMessage handleReenter(String roomKey, String nickname, Long roomId) {
        log.debug("Balance 재입장 처리: roomKey={}, nickname={}, roomId={}", roomKey, nickname, roomId);
        
        // 재입장 시에는 특별한 메시지 없이 null 반환
        // (Balance 게임에서는 재입장 시 특별한 처리가 필요하지 않음)
        return null;
    }

    /**
     * 사용자 연결 해제 처리
     */
    public ChatMessage handleUserDisconnection(String roomKey, String nickname, Long roomId) {
        log.debug("Balance 사용자 연결 해제 처리 시작: roomKey={}, nickname={}, roomId={}", roomKey, nickname, roomId);

        // 방 정보 조회
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + roomKey));

        // 현재 온라인 멤버 수 조회 (연결 해제 후 상태)
        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        
        log.debug("Balance 온라인 사용자 수 조회 완료: roomKey={}, onlineUserCount={}, maxMember={}",
                roomKey, onlineUserCount, room.getMaxMember());

        // 멤버 퇴장 알림 메시지 생성
        ChatMessage leaveMessage = BalanceChatMessage.leave(nickname, onlineUserCount, room.getMaxMember());

        log.debug("Balance 연결 해제 메시지 생성 완료: nickname={}, type={}, content={}",
                nickname, leaveMessage.getType(), leaveMessage.getContent());

        return leaveMessage;
    }

    /**
     * 멤버 점수 초기화
     */
    private void initializeMemberScores(Long roomId) {
        // 해당 방의 모든 멤버 조회
        List<Member> members = memberRepository.findByRoomId(roomId);
        
        List<String> memberNames = members.stream()
                .map(Member::getNickname)
                .toList();
        
        List<Long> memberIds = members.stream()
                .map(Member::getId)
                .toList();
        
        // 점수 초기화
        balanceScoreService.initializeMemberScores(roomId, memberNames, memberIds);
        
        log.debug("Balance 멤버 점수 초기화 완료: roomId={}, memberCount={}", roomId, members.size());
    }
} 