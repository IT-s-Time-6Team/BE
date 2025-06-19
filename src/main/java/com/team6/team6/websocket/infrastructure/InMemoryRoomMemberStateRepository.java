package com.team6.team6.websocket.infrastructure;

import com.team6.team6.websocket.domain.RoomMemberStateRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRoomMemberStateRepository implements RoomMemberStateRepository {
    // 사용자 방 입장 기록을 위한 맵 (방ID -> {멤버ID -> 온라인 상태})
    private final Map<String, Map<String, Boolean>> roomMemberRegistry = new ConcurrentHashMap<>();

    // 첫 연결 여부를 추적하는 맵 (방ID -> {멤버ID -> 첫 연결 여부})
    private final Map<String, Map<String, Boolean>> firstConnectionRegistry = new ConcurrentHashMap<>();

    @Override
    public boolean isUserInRoom(String roomKey, String nickname) {
        return roomMemberRegistry.containsKey(roomKey) &&
               roomMemberRegistry.get(roomKey).containsKey(nickname);
    }

    @Override
    public void registerUserInRoom(String roomKey, String nickname) {
        roomMemberRegistry
            .computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>())
            .put(nickname, true);

        // 첫 연결로 설정
        firstConnectionRegistry
                .computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>())
                .put(nickname, true);
    }

    @Override
    public Map<String, Boolean> getRoomMembers(String roomKey) {
        return roomMemberRegistry.computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>());
    }

    @Override
    public boolean isUserOnline(String roomKey, String nickname) {
        if (!isUserInRoom(roomKey, nickname)) {
            return false;
        }
        return Boolean.TRUE.equals(roomMemberRegistry.get(roomKey).get(nickname));
    }

    @Override
    public void setUserOnline(String roomKey, String nickname) {
        roomMemberRegistry
                .computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>())
                .put(nickname, true);
    }

    @Override
    public void setUserOffline(String roomKey, String nickname) {
        if (isUserInRoom(roomKey, nickname)) {
            roomMemberRegistry.get(roomKey).put(nickname, false);
        }
    }

    @Override
    public int getOnlineUserCount(String roomKey) {
        if (!roomMemberRegistry.containsKey(roomKey)) {
            return 0;
        }

        return (int) roomMemberRegistry.get(roomKey).entrySet().stream()
                .filter(Map.Entry::getValue)
                .count();
    }

    @Override
    public boolean isFirstConnection(String roomKey, String nickname) {
        if (!firstConnectionRegistry.containsKey(roomKey)) {
            return true; // 방 자체가 없으면 첫 연결
        }

        Map<String, Boolean> roomFirstConnections = firstConnectionRegistry.get(roomKey);
        return !roomFirstConnections.containsKey(nickname) ||
                Boolean.TRUE.equals(roomFirstConnections.get(nickname));
    }

    @Override
    public void setFirstConnection(String roomKey, String nickname, boolean isFirst) {
        firstConnectionRegistry
                .computeIfAbsent(roomKey, k -> new ConcurrentHashMap<>())
                .put(nickname, isFirst);
    }

    @Override
    public void removeUser(String roomKey, String nickname) {
        if (roomMemberRegistry.containsKey(roomKey)) {
            roomMemberRegistry.get(roomKey).remove(nickname);
        }

        if (firstConnectionRegistry.containsKey(roomKey)) {
            firstConnectionRegistry.get(roomKey).remove(nickname);
        }
    }
} 