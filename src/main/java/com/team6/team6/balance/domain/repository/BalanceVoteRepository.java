package com.team6.team6.balance.domain.repository;

import com.team6.team6.balance.entity.BalanceVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceVoteRepository extends JpaRepository<BalanceVote, Long> {
    
    List<BalanceVote> findByRoomIdAndVotingRound(Long roomId, Integer votingRound);
    
    boolean existsByRoomIdAndMemberIdAndVotingRound(Long roomId, Long memberId, Integer votingRound);
    
    boolean existsByRoomIdAndVoterNameAndVotingRound(Long roomId, String voterName, Integer votingRound);
    
    List<BalanceVote> findByRoomId(Long roomId);
    
    void deleteByRoomId(Long roomId);
    
    // 특정 사용자의 특정 라운드 투표 조회
    List<BalanceVote> findByRoomIdAndVoterNameAndVotingRound(Long roomId, String voterName, Integer votingRound);
} 