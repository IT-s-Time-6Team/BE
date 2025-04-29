package com.team6.team6.question.service;

import com.team6.team6.question.domain.KeywordLockManager;
import com.team6.team6.question.domain.QuestionGenerator;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.Questions;
import com.team6.team6.question.dto.QuestionResponse;
import com.team6.team6.question.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

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

    public List<QuestionResponse> getRandomQuestions(String keyword) {
        Questions questions = Questions.of(questionRepository.findAllByKeyword(keyword));
        return questions.getRandomSubset(DEFAULT_QUESTION_COUNT).stream()
                .map(QuestionResponse::from)
                .toList();
    }
}
