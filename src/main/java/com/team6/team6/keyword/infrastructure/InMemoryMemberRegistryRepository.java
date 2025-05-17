package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMemberRegistryRepository implements MemberRegistryRepository {
    // 사용자 방 입장 기록을 위한 맵 (방ID -> {멤버ID -> 온라인 상태})
    private final Map<String, Map<String, Boolean>> roomMemberRegistry = new ConcurrentHashMap<>();

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
}
