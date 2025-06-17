package com.team6.team6.websocket.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 웹소켓 방의 멤버 상태를 관리하는 매니저
 */
@Service
@RequiredArgsConstructor
public class RoomMemberStateManager {

    private final RoomMemberStateRepository repository;

    /**
     * 사용자 온라인 상태 관리
     */
    public void handleUserOnlineStatus(String roomKey, String nickname, boolean isOnline) {
        if (isOnline) {
            if (repository.isUserInRoom(roomKey, nickname)) {
                // 기존에 있음 - 온라인으로 변경 (재연결)
                repository.setUserOnline(roomKey, nickname);
                // 재연결이므로 첫 연결이 아님
                repository.setFirstConnection(roomKey, nickname, false);
            } else {
                // 없음 - 등록 후 온라인 변경 (첫 연결)
                repository.registerUserInRoom(roomKey, nickname);
                // registerUserInRoom에서 이미 첫 연결로 설정됨
            }
        } else {
            // 연결 해제시 - 오프라인 전환
            repository.setUserOffline(roomKey, nickname);
            // 연결이 완전히 끊어지면 다음 연결을 첫 연결로 처리하기 위해 설정
            repository.setFirstConnection(roomKey, nickname, true);
        }
    }

    /**
     * 첫 연결인지 확인
     */
    public boolean isFirstConnection(String roomKey, String nickname) {
        return repository.isFirstConnection(roomKey, nickname);
    }

    /**
     * 첫 연결 여부를 false로 변경
     */
    public void markNotFirstConnection(String roomKey, String nickname) {
        repository.setFirstConnection(roomKey, nickname, false);
    }

    public int getOnlineUserCount(String roomKey) {
        return repository.getOnlineUserCount(roomKey);
    }

    public boolean isUserInRoom(String roomKey, String nickname) {
        return repository.isUserInRoom(roomKey, nickname);
    }

    public boolean isUserOnline(String roomKey, String nickname) {
        return repository.isUserOnline(roomKey, nickname);
    }

    public void registerUserInRoom(String roomKey, String nickname) {
        repository.registerUserInRoom(roomKey, nickname);
    }

    public void setUserOnline(String roomKey, String nickname) {
        repository.setUserOnline(roomKey, nickname);
    }

    public void setUserOffline(String roomKey, String nickname) {
        repository.setUserOffline(roomKey, nickname);
    }

    public Map<String, Boolean> getRoomMembers(String roomKey) {
        return repository.getRoomMembers(roomKey);
    }

    /**
     * 사용자를 완전히 제거 (방 나가기 등)
     */
    public void removeUser(String roomKey, String nickname) {
        repository.removeUser(roomKey, nickname);
    }
} 