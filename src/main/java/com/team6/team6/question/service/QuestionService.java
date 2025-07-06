package com.team6.team6.question.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.keyword.domain.KeywordPreprocessor;
import com.team6.team6.keyword.entity.KeywordGroup;
import com.team6.team6.question.domain.QuestionRepository;
import com.team6.team6.question.domain.Questions;
import com.team6.team6.question.dto.QuestionResponse;
import com.team6.team6.question.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final QuestionRepository questionRepository;
    private final KeywordPreprocessor keywordPreprocessor;

    @Transactional
    public void saveNewQuestions(String keyword, List<String> generated, KeywordGroup keywordGroup) {
        log.info("질문 저장 시작: 키워드={}", keyword);

        // Double-Checked Locking: 락을 획득하고 이 트랜잭션이 시작되기까지의 짧은 시간 동안
        // 다른 스레드가 데이터를 이미 저장했을 수 있으므로, 최종적으로 한 번 더 확인
        if (questionRepository.existsByKeyword(keyword)) {
            log.warn("이미 질문이 존재하여 저장하지 않음: 키워드={}", keyword);
            return;
        }

        List<Question> questions = generated.stream()
                .map(q -> Question.of(keyword, q, keywordGroup))
                .toList();
        questionRepository.saveAll(questions);
        log.info("질문 저장 완료: 키워드={}, 생성된 질문 수={}", keyword, questions.size());
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getRandomQuestions(String keyword) {
        String preprocessedKeyword = keywordPreprocessor.preprocess(keyword);
        Questions questions = Questions.of(questionRepository.findAllByKeyword(preprocessedKeyword));
        if (questions.isEmpty()) {
            throw new NotFoundException("해당 키워드에 대한 질문이 존재하지 않습니다: " + preprocessedKeyword);
        }
        return questions.getRandomSubset(DEFAULT_QUESTION_COUNT).stream()
                .map(QuestionResponse::from)
                .toList();
    }
}
