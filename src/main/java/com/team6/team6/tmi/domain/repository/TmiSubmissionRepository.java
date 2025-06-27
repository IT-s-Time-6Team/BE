package com.team6.team6.tmi.domain.repository;

import com.team6.team6.tmi.entity.TmiSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TmiSubmissionRepository extends JpaRepository<TmiSubmission, Long> {
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);

    List<TmiSubmission> findByRoomId(Long roomId);

    Optional<TmiSubmission> findByRoomIdAndDisplayOrder(Long roomId, int displayOrder);
}
