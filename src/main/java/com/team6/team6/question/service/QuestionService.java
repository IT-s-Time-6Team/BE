package com.team6.team6.question.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.question.domain.KeywordLockManager;
import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.Questions;
import com.team6.team6.question.dto.QuestionResponse;
import com.team6.team6.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final QuestionRepository questionRepository;
    private final QuestionGenerator questionGenerator;
    private final KeywordLockManager lockManager;

    @Transactional
    @Async
    public void generateQuestions(String keyword) {
        if (questionRepository.existsByKeyword(keyword) || !lockManager.tryLock(keyword)) {
            return;
        }

        List<String> generated = questionGenerator.generateQuestions(keyword);
        List<Question> questions = generated.stream()
                .map(q -> Question.of(keyword, q))
                .toList();
        questionRepository.saveAll(questions);
        unlockAfterCommit(keyword);
    }

    public List<QuestionResponse> getRandomQuestions(String keyword) {
        Questions questions = Questions.of(questionRepository.findAllByKeyword(keyword));
        if (questions.isEmpty()) {
            throw new NotFoundException("해당 키워드에 대한 질문이 존재하지 않습니다: " + keyword);
        }
        return questions.getRandomSubset(DEFAULT_QUESTION_COUNT).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    private void unlockAfterCommit(String keyword) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                lockManager.unlock(keyword);
            }

            @Override
            public void afterCompletion(int status) {
                // 커밋이 아닌 종료(ex. rollback)인 경우에도 안전하게 락 해제
                if (status != STATUS_COMMITTED) {
                    lockManager.unlock(keyword);
                }
            }
        });
    }
}
