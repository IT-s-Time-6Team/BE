package com.team6.team6.balance.domain.repository;

import com.team6.team6.balance.entity.BalanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface BalanceSessionRepository extends JpaRepository<BalanceSession, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT bs FROM BalanceSession bs WHERE bs.roomId = :roomId")
    Optional<BalanceSession> findByRoomIdWithLock(@Param("roomId") Long roomId);
    
    Optional<BalanceSession> findByRoomId(Long roomId);
} 