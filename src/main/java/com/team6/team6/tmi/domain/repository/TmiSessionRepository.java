package com.team6.team6.tmi.domain.repository;

import com.team6.team6.tmi.entity.TmiSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TmiSessionRepository extends JpaRepository<TmiSession, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TmiSession ts WHERE ts.roomId = :roomId")
    Optional<TmiSession> findByRoomIdWithLock(@Param("roomId") Long roomId);
}
