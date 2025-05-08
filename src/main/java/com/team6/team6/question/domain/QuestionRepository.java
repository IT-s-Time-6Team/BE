package com.team6.team6.question.domain;

import com.team6.team6.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByKeyword(String keyword);

    List<Question> findAllByKeyword(String keyword);
}
