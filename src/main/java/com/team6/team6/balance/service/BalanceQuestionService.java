package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceSessionQuestions;
import com.team6.team6.balance.domain.repository.BalanceSessionQuestionRepository;
import com.team6.team6.balance.domain.repository.BalanceQuestionRepository;
import com.team6.team6.balance.dto.BalanceQuestionResponse;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import com.team6.team6.balance.entity.BalanceQuestion;
import com.team6.team6.balance.entity.BalanceSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BalanceQuestionService {

    private final BalanceQuestionRepository balanceQuestionRepository;
    private final BalanceSessionQuestionRepository balanceSessionQuestionRepository;
    private final BalanceSessionService balanceSessionService;

    @Transactional
    public void selectRandomQuestionsForRoom(Long roomId, int questionCount) {
        // 이미 선택된 문제가 있는지 확인
        if (balanceSessionQuestionRepository.existsByRoomId(roomId)) {
            log.warn("이미 방에 문제가 선택되어 있습니다: roomId={}", roomId);
            return;
        }

        // 모든 문제 조회 후 랜덤 선택
        List<BalanceQuestion> allQuestions = balanceQuestionRepository.findAllQuestions();
        
        if (allQuestions.size() < questionCount) {
            throw new IllegalStateException("요청한 문제 수보다 DB에 저장된 문제가 적습니다. 요청: " + questionCount + ", 저장: " + allQuestions.size());
        }

        // 랜덤으로 섞기
        Collections.shuffle(allQuestions);
        List<BalanceQuestion> selectedQuestions = allQuestions.subList(0, questionCount);

        // BalanceSessionQuestion으로 변환하여 저장
        List<BalanceSessionQuestion> gameQuestions = IntStream.range(0, selectedQuestions.size())
                .mapToObj(i -> {
                    BalanceQuestion question = selectedQuestions.get(i);
                    return BalanceSessionQuestion.create(
                            roomId,
                            question.getId(),
                            question.getQuestionA(),
                            question.getQuestionB(),
                            i
                    );
                })
                .toList();

        balanceSessionQuestionRepository.saveAll(gameQuestions);
        log.info("방에 대한 밸런스 문제 선택 완료: roomId={}, questionCount={}", roomId, questionCount);
    }

    public BalanceQuestionResponse getCurrentQuestion(Long roomId) {
        BalanceSession session = balanceSessionService.findSessionByRoomId(roomId);
        
        BalanceSessionQuestion currentQuestion = getCurrentQuestionByDisplayOrder(roomId, session.getCurrentQuestionIndex());
        
        return BalanceQuestionResponse.of(
                currentQuestion.getQuestionA(),
                currentQuestion.getQuestionB(),
                session.getCurrentQuestionIndex(),
                session.getTotalQuestions()
        );
    }

    public BalanceSessionQuestion getQuestionByDisplayOrder(Long roomId, int order) {
        return balanceSessionQuestionRepository.findByRoomIdAndDisplayOrder(roomId, order)
                .orElseThrow(() -> new IllegalArgumentException("해당 순서의 문제를 찾을 수 없습니다: roomId=" + roomId + ", order=" + order));
    }

    public BalanceSessionQuestion getCurrentQuestionByDisplayOrder(Long roomId, int order) {
        return getQuestionByDisplayOrder(roomId, order);
    }

    public List<BalanceSessionQuestion> getAllQuestionsForRoom(Long roomId) {
        return balanceSessionQuestionRepository.findByRoomIdOrderByDisplayOrder(roomId);
    }

    public BalanceSessionQuestions getBalanceSessionQuestions(Long roomId) {
        List<BalanceSessionQuestion> questions = getAllQuestionsForRoom(roomId);
        return BalanceSessionQuestions.from(questions);
    }
} 