package com.team6.team6.room.infrastructure;

import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepository {

    private final RoomJpaRepository jpaRepository;
    private final RoomQueryDslRepository queryDslRepository;

    @Override
    public Optional<Room> findByRoomKey(String roomKey) {
        return jpaRepository.findByRoomKey(roomKey);
    }

    @Override
    public Optional<Room> findByRoomKeyWithLock(String roomKey) {
        return jpaRepository.findByRoomKeyWithLock(roomKey);
    }

    @Override
    public Room save(Room room) {
        return jpaRepository.save(room);
    }

    @Override
    public List<MemberKeywordCount> findAllMemberKeywordCountsInRoom(String roomKey) {
        return queryDslRepository.findAllMemberKeywordCountsInRoom(roomKey);
    }

    @Override
    public List<MemberKeywordCount> findAllMemberSharedKeywordCountsInRoom(String roomKey, List<String> sharedKeywords) {
        return queryDslRepository.findAllMemberSharedKeywordCountsInRoom(roomKey, sharedKeywords);
    }
}
