package com.team6.team6.keyword.domain.repository;

import com.team6.team6.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByRoomIdAndMemberId(Long loomId, Long memberId);
}