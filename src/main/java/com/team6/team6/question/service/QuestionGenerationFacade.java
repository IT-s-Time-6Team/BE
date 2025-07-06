package com.team6.team6.question.service;

import com.team6.team6.keyword.entity.KeywordGroup;
import com.team6.team6.question.domain.KeywordLockManager;
import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationFacade {

    private final QuestionRepository questionRepository;
    private final QuestionGenerator questionGenerator;
    private final KeywordLockManager lockManager;
    private final QuestionService questionService;

    @Async
    public void generateQuestions(String keyword, KeywordGroup keywordGroup) {
        // 1. 락을 잡기 전, DB에 이미 데이터가 있는지 간단히 확인
        if (questionRepository.existsByKeyword(keyword)) {
            return;
        }

        // 2. 분산 락을 시도
        if (!lockManager.tryLock(keyword)) {
            log.warn("키워드 '{}'에 대한 락 획득 실패", keyword);
            return;
        }

        try {
            // 3. 외부 API를 호출
            log.debug("질문 생성 API 호출 시작: {}", keyword);
            List<String> generated = questionGenerator.generateQuestions(keyword);
            log.debug("생성된 질문 수: {}", generated.size());

            // 4. 생성된 데이터를 DB에 저장하기 위해 트랜잭션 메소드 호출
            questionService.saveNewQuestions(keyword, generated, keywordGroup);
        } finally {
            // 5. 작업이 성공하든 실패하든, 락을 반드시 해제
            lockManager.unlock(keyword);
            log.info("키워드 '{}' 락 해제", keyword);
        }
    }
}
