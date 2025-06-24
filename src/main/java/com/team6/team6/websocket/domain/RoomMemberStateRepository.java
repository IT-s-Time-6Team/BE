package com.team6.team6.websocket.domain;

import java.util.Map;

/**
 * 웹소켓 방의 멤버 상태를 관리하는 레포지토리 인터페이스
 */
public interface RoomMemberStateRepository {
    boolean isUserInRoom(String roomKey, String nickname);

    void registerUserInRoom(String roomKey, String nickname);

    Map<String, Boolean> getRoomMembers(String roomKey);

    boolean isUserOnline(String roomKey, String nickname);

    void setUserOnline(String roomKey, String nickname);

    void setUserOffline(String roomKey, String nickname);

    int getOnlineUserCount(String roomKey);

    // 첫 연결 여부 관리 메서드 추가
    boolean isFirstConnection(String roomKey, String nickname);

    void setFirstConnection(String roomKey, String nickname, boolean isFirst);

    void removeUser(String roomKey, String nickname);
} 