package com.team6.team6.member.domain;

import com.team6.team6.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNicknameAndRoomId(String nickname, Long roomId);
    long countByRoomId(Long roomId);

    // 방의 모든 멤버 조회 메소드 추가
    List<Member> findByRoomId(Long roomId);
}