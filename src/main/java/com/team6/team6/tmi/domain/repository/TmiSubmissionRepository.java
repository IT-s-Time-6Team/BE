package com.team6.team6.tmi.domain.repository;

import com.team6.team6.tmi.entity.TmiSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmiSubmissionRepository extends JpaRepository<TmiSubmission, Long> {
    boolean existsByRoomIdAndMemberId(Long roomId, Long memberId);
}
