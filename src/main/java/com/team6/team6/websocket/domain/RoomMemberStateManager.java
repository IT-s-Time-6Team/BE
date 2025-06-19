package com.team6.team6.websocket.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 웹소켓 방의 멤버 상태를 관리하는 매니저
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoomMemberStateManager {

    private final RoomMemberStateRepository repository;

    /**
     * 사용자 온라인 상태 관리
     */
    public void handleUserOnlineStatus(String roomKey, String nickname, boolean isOnline) {
        log.debug("사용자 온라인 상태 관리 시작: roomKey={}, nickname={}, isOnline={}",
                roomKey, nickname, isOnline);

        if (isOnline) {
            boolean wasInRoom = repository.isUserInRoom(roomKey, nickname);
            log.debug("사용자 방 존재 여부 확인: roomKey={}, nickname={}, wasInRoom={}",
                    roomKey, nickname, wasInRoom);

            if (wasInRoom) {
                // 기존에 있음 - 온라인으로 변경 (재연결)
                repository.setUserOnline(roomKey, nickname);
                // 재연결이므로 첫 연결이 아님
                repository.setFirstConnection(roomKey, nickname, false);

                log.debug("재연결 처리 완료: roomKey={}, nickname={}, isFirstConnection=false",
                        roomKey, nickname);
            } else {
                // 없음 - 등록 후 온라인 변경 (첫 연결)
                repository.registerUserInRoom(roomKey, nickname);
                // registerUserInRoom에서 이미 첫 연결로 설정됨

                log.debug("신규 사용자 등록 완료: roomKey={}, nickname={}, isFirstConnection=true",
                        roomKey, nickname);
            }
        } else {
            // 연결 해제시 - 오프라인 전환
            repository.setUserOffline(roomKey, nickname);
            // 연결이 완전히 끊어지면 다음 연결을 첫 연결로 처리하기 위해 설정
            repository.setFirstConnection(roomKey, nickname, true);

            log.debug("연결 해제 처리 완료: roomKey={}, nickname={}, isFirstConnection=true",
                    roomKey, nickname);
        }

        int currentOnlineCount = repository.getOnlineUserCount(roomKey);
        log.debug("사용자 온라인 상태 관리 완료: roomKey={}, nickname={}, isOnline={}, currentOnlineCount={}",
                roomKey, nickname, isOnline, currentOnlineCount);
    }

    /**
     * 첫 연결인지 확인
     */
    public boolean isFirstConnection(String roomKey, String nickname) {
        boolean isFirst = repository.isFirstConnection(roomKey, nickname);
        log.debug("첫 연결 여부 확인: roomKey={}, nickname={}, isFirstConnection={}",
                roomKey, nickname, isFirst);
        return isFirst;
    }

    /**
     * 첫 연결 여부를 false로 변경
     */
    public void markNotFirstConnection(String roomKey, String nickname) {
        log.debug("첫 연결 상태 변경: roomKey={}, nickname={}, isFirstConnection=false",
                roomKey, nickname);
        repository.setFirstConnection(roomKey, nickname, false);
    }

    public int getOnlineUserCount(String roomKey) {
        int count = repository.getOnlineUserCount(roomKey);
        log.debug("온라인 사용자 수 조회: roomKey={}, onlineUserCount={}", roomKey, count);
        return count;
    }

    public boolean isUserInRoom(String roomKey, String nickname) {
        boolean inRoom = repository.isUserInRoom(roomKey, nickname);
        log.debug("사용자 방 존재 여부: roomKey={}, nickname={}, isUserInRoom={}",
                roomKey, nickname, inRoom);
        return inRoom;
    }

    public boolean isUserOnline(String roomKey, String nickname) {
        boolean online = repository.isUserOnline(roomKey, nickname);
        log.debug("사용자 온라인 상태: roomKey={}, nickname={}, isUserOnline={}",
                roomKey, nickname, online);
        return online;
    }

    public void registerUserInRoom(String roomKey, String nickname) {
        log.debug("사용자 방 등록: roomKey={}, nickname={}", roomKey, nickname);
        repository.registerUserInRoom(roomKey, nickname);
    }

    public void setUserOnline(String roomKey, String nickname) {
        log.debug("사용자 온라인 설정: roomKey={}, nickname={}", roomKey, nickname);
        repository.setUserOnline(roomKey, nickname);
    }

    public void setUserOffline(String roomKey, String nickname) {
        log.debug("사용자 오프라인 설정: roomKey={}, nickname={}", roomKey, nickname);
        repository.setUserOffline(roomKey, nickname);
    }

    public Map<String, Boolean> getRoomMembers(String roomKey) {
        Map<String, Boolean> members = repository.getRoomMembers(roomKey);
        log.debug("방 멤버 목록 조회: roomKey={}, memberCount={}, members={}",
                roomKey, members.size(), members);
        return members;
    }

    /**
     * 사용자를 완전히 제거 (방 나가기 등)
     */
    public void removeUser(String roomKey, String nickname) {
        log.debug("사용자 완전 제거: roomKey={}, nickname={}", roomKey, nickname);
        repository.removeUser(roomKey, nickname);
        log.debug("사용자 완전 제거 완료: roomKey={}, nickname={}", roomKey, nickname);
    }
} 