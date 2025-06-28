package com.team6.team6.tmi.domain.repository;

import com.team6.team6.tmi.entity.TmiVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TmiVoteRepository extends JpaRepository<TmiVote, Long> {
    boolean existsByRoomIdAndVoterNameAndVotingRound(Long roomId, String voterName, int votingRound);

    List<TmiVote> findByRoomIdAndVotingRound(Long roomId, int latestCompletedRound);

    List<TmiVote> findAllByRoomId(Long roomId);
}
