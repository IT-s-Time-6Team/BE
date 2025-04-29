package com.team6.team6.member.domain;

import com.team6.team6.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNicknameAndRoomId(String nickname, Long roomId);
    long countByRoomId(Long roomId);
}