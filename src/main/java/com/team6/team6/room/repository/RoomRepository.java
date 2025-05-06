package com.team6.team6.room.repository;

import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository {

    Optional<Room> findByRoomKey(String roomKey);

    Optional<Room> findByRoomKeyWithLock(String roomKey);

    Room save(Room room);

    List<MemberKeywordCount> findMembersWithMostKeywordsInRoom(String roomKey);

    List<MemberKeywordCount> findMembersWithMostSharedKeywordsInRoom(String roomKey, List<String> sharedKeywords);
}
