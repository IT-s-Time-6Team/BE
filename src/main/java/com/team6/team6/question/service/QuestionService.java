package com.team6.team6.question.service;

import com.team6.team6.question.domain.KeywordLockManager;
import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionGenerator questionGenerator;
    private final KeywordLockManager lockManager;

    @Async
    public void generateQuestions(String keyword) {
        if (questionRepository.existsByKeyword(keyword) || !lockManager.tryLock(keyword)) {
            return;
        }

        try {
            List<String> generated = questionGenerator.generateQuestions(keyword);
            List<Question> questions = generated.stream()
                    .map(q -> Question.of(keyword, q))
                    .toList();
            questionRepository.saveAll(questions);
        } finally {
            lockManager.unlock(keyword);
        }
    }

    public List<Question> getRandomQuestions(String keyword) {
        List<Question> all = questionRepository.findAllByKeyword(keyword);

        if (all.size() <= 10) return all;

        Collections.shuffle(all);
        return all.subList(0, 10);
    }
}
