package com.team6.team6.balance.domain.repository;

import com.team6.team6.balance.entity.BalanceQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BalanceQuestionRepository extends JpaRepository<BalanceQuestion, Long> {
    
    // 랜덤 문제 선택은 서비스 레이어에서 처리
    @Query("SELECT bq FROM BalanceQuestion bq")
    List<BalanceQuestion> findAllQuestions();
    
    long count();
} 