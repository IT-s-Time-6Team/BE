package com.team6.team6.room.infrastructure;

import com.team6.team6.room.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomJpaRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomKey(String roomKey);

    Room save(Room room);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.roomKey = :roomKey")
    Optional<Room> findByRoomKeyWithLock(@Param("roomKey") String roomKey);
}
