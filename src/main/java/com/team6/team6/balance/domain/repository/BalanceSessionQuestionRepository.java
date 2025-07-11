package com.team6.team6.balance.domain.repository;

import com.team6.team6.balance.entity.BalanceSessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceSessionQuestionRepository extends JpaRepository<BalanceSessionQuestion, Long> {
    
    List<BalanceSessionQuestion> findByRoomIdOrderByDisplayOrder(Long roomId);
    
    Optional<BalanceSessionQuestion> findByRoomIdAndDisplayOrder(Long roomId, Integer displayOrder);
    
    boolean existsByRoomId(Long roomId);
    
    void deleteByRoomId(Long roomId);
} 