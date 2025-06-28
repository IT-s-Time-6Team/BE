package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * WebSocket 구독 관련 서비스 로직을 처리하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordWebSocketSubscribeService {

    private final RoomMemberStateManager roomMemberStateManager;
    private final RoomKeywordManager keywordManager;
    private final MessagePublisher messagePublisher;
    private final KeywordService keywordService;
    private final MemberRepository memberRepository;

    public ChatMessage handleUserSubscription(String roomKey, String nickname, Long roomId, Long memberId) {
        log.debug("사용자 구독 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        // 첫 연결인지 확인 (연결 시점에서 설정된 상태 확인)
        boolean isFirstConnection = roomMemberStateManager.isFirstConnection(roomKey, nickname);

        log.debug("연결 상태 확인: roomKey={}, nickname={}, isFirstConnection={}",
                roomKey, nickname, isFirstConnection);

        ChatMessage result = isFirstConnection
                ? handleEnter(roomKey, nickname, roomId)
                : handleReenter(roomKey, nickname, roomId, memberId);

        log.debug("사용자 구독 처리 완료: roomKey={}, nickname={}, messageType={}",
                roomKey, nickname, result.getType());

        return result;
    }

    private ChatMessage handleEnter(String roomKey, String nickname, Long roomId) {
        log.debug("첫 입장 처리 시작: roomKey={}, nickname={}, roomId={}", roomKey, nickname, roomId);

        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        List<KeywordChatMessage.RoomMemberInfo> roomMembers = getRoomMemberInfos(roomId);

        log.debug("온라인 사용자 수 및 멤버 정보 조회 완료: roomKey={}, onlineUserCount={}, memberCount={}",
                roomKey, onlineUserCount, roomMembers.size());

        ChatMessage enterMessage = KeywordChatMessage.enter(nickname, onlineUserCount, roomMembers);

        log.debug("입장 메시지 생성 완료: nickname={}, type={}, content={}, memberCount={}",
                nickname, enterMessage.getType(), enterMessage.getContent(), roomMembers.size());

        return enterMessage;
    }

    private ChatMessage handleReenter(String roomKey, String nickname, Long roomId, Long memberId) {
        log.debug("재입장 처리 시작: roomKey={}, nickname={}, roomId={}, memberId={}",
                roomKey, nickname, roomId, memberId);

        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        List<KeywordChatMessage.RoomMemberInfo> roomMembers = getRoomMemberInfos(roomId);

        log.debug("온라인 사용자 수 및 멤버 정보 조회 완료: roomKey={}, onlineUserCount={}, memberCount={}",
                roomKey, onlineUserCount, roomMembers.size());

        List<Keyword> keywords = keywordService.getUserKeywords(roomId, memberId);

        log.debug("사용자 키워드 조회 완료: roomId={}, memberId={}, keywordCount={}",
                roomId, memberId, keywords.size());

        List<String> uniqueKeywords = keywords.stream()
                .map(Keyword::getKeyword)
                .distinct()
                .collect(Collectors.toList());

        log.debug("고유 키워드 목록 생성: roomId={}, memberId={}, uniqueKeywordCount={}, keywords={}",
                roomId, memberId, uniqueKeywords.size(), uniqueKeywords);

        ChatMessage reenterMessage = KeywordChatMessage.reenter(nickname, onlineUserCount, uniqueKeywords, roomMembers);

        log.debug("재입장 메시지 생성 완료: nickname={}, type={}, content={}, userCount={}, keywordCount={}, memberCount={}",
                nickname, reenterMessage.getType(), reenterMessage.getContent(),
                onlineUserCount, uniqueKeywords.size(), roomMembers.size());

        return reenterMessage;
    }

    /**
     * 방의 모든 멤버 정보를 조회하는 메소드
     */
    private List<KeywordChatMessage.RoomMemberInfo> getRoomMemberInfos(Long roomId) {
        log.debug("방 멤버 정보 조회 시작: roomId={}", roomId);

        List<Member> members = memberRepository.findByRoomId(roomId);

        List<KeywordChatMessage.RoomMemberInfo> roomMemberInfos = members.stream()
                .map(member -> new KeywordChatMessage.RoomMemberInfo(
                        member.getNickname(),
                        member.getCharacter(),
                        member.isLeader()
                ))
                .collect(Collectors.toList());

        log.debug("방 멤버 정보 조회 완료: roomId={}, memberCount={}", roomId, roomMemberInfos.size());

        return roomMemberInfos;
    }

    /**
     * 사용자 연결 해제 처리
     */
    public ChatMessage handleUserDisconnection(String roomKey, String nickname, Long roomId) {
        log.debug("사용자 연결 해제 처리 시작: roomKey={}, nickname={}, roomId={}", roomKey, nickname, roomId);

        int onlineUserCount = roomMemberStateManager.getOnlineUserCount(roomKey);
        List<KeywordChatMessage.RoomMemberInfo> roomMembers = getRoomMemberInfos(roomId);

        log.debug("온라인 사용자 수 및 멤버 정보 조회 완료: roomKey={}, onlineUserCount={}, memberCount={}",
                roomKey, onlineUserCount, roomMembers.size());

        ChatMessage leaveMessage = KeywordChatMessage.leave(nickname, onlineUserCount, roomMembers);

        log.debug("연결 해제 메시지 생성 완료: nickname={}, type={}, content={}, memberCount={}",
                nickname, leaveMessage.getType(), leaveMessage.getContent(), roomMembers.size());

        return leaveMessage;
    }

    /**
     * 키워드 분석 결과를 발행하는 메서드
     *
     * @param roomKey 방 식별자
     * @param roomId  방 ID
     */
    public void publishAnalysisResults(String roomKey, Long roomId) {
        log.debug("키워드 분석 결과 발행 시작: roomKey={}, roomId={}", roomKey, roomId);

        // 마지막 키워드 분석 결과 전송
        List<AnalysisResult> results = keywordManager.getAnalysisResult(roomId);

        log.debug("키워드 분석 결과 조회 완료: roomKey={}, roomId={}, resultCount={}",
                roomKey, roomId, results.size());

        if (!results.isEmpty()) {
            messagePublisher.publishKeywordAnalysisResult(roomKey, results);

            log.debug("키워드 분석 결과 발행 완료: roomKey={}, roomId={}, resultCount={}",
                    roomKey, roomId, results.size());
        } else {
            log.debug("발행할 키워드 분석 결과가 없음: roomKey={}, roomId={}", roomKey, roomId);
        }
    }
}
