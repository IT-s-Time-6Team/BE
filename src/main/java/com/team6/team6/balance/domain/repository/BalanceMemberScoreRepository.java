package com.team6.team6.balance.domain.repository;

import com.team6.team6.balance.entity.BalanceMemberScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalanceMemberScoreRepository extends JpaRepository<BalanceMemberScore, Long> {
    
    List<BalanceMemberScore> findByRoomIdOrderByCurrentScoreDesc(Long roomId);
    
    Optional<BalanceMemberScore> findByRoomIdAndMemberId(Long roomId, Long memberId);
    
    Optional<BalanceMemberScore> findByRoomIdAndMemberName(Long roomId, String memberName);
    
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);
    
    void deleteByRoomId(Long roomId);
} 