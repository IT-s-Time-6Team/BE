package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceSessionQuestions;
import com.team6.team6.balance.domain.repository.BalanceQuestionRepository;
import com.team6.team6.balance.domain.repository.BalanceSessionQuestionRepository;
import com.team6.team6.balance.dto.BalanceQuestionResponse;
import com.team6.team6.balance.entity.BalanceQuestion;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BalanceQuestionService 테스트")
class BalanceQuestionServiceTest {

    @Mock
    private BalanceQuestionRepository balanceQuestionRepository;

    @Mock
    private BalanceSessionQuestionRepository balanceSessionQuestionRepository;

    @Mock
    private BalanceSessionService balanceSessionService;

    @InjectMocks
    private BalanceQuestionService balanceQuestionService;

    @Test
    @DisplayName("방에 대한 랜덤 문제 선택 - 정상 케이스")
    void selectRandomQuestionsForRoom_Success_Test() {
        // given
        Long roomId = 1L;
        int questionCount = 3;
        
        BalanceQuestion question1 = createBalanceQuestion(1L, "치킨", "피자");
        BalanceQuestion question2 = createBalanceQuestion(2L, "개", "고양이");
        BalanceQuestion question3 = createBalanceQuestion(3L, "여행", "휴식");
        BalanceQuestion question4 = createBalanceQuestion(4L, "영화", "책");
        List<BalanceQuestion> allQuestions = new ArrayList<>(List.of(question1, question2, question3, question4));
        
        when(balanceSessionQuestionRepository.existsByRoomId(roomId)).thenReturn(false);
        when(balanceQuestionRepository.findAllQuestions()).thenReturn(allQuestions);

        // when
        balanceQuestionService.selectRandomQuestionsForRoom(roomId, questionCount);

        // then
        verify(balanceSessionQuestionRepository).saveAll(anyList());
        verify(balanceSessionQuestionRepository).existsByRoomId(roomId);
        verify(balanceQuestionRepository).findAllQuestions();
    }

    @Test
    @DisplayName("방에 대한 랜덤 문제 선택 - 이미 선택된 문제가 있는 경우")
    void selectRandomQuestionsForRoom_AlreadyExists_Test() {
        // given
        Long roomId = 1L;
        int questionCount = 3;
        
        when(balanceSessionQuestionRepository.existsByRoomId(roomId)).thenReturn(true);

        // when
        balanceQuestionService.selectRandomQuestionsForRoom(roomId, questionCount);

        // then
        verify(balanceSessionQuestionRepository).existsByRoomId(roomId);
        verify(balanceQuestionRepository, never()).findAllQuestions();
        verify(balanceSessionQuestionRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("방에 대한 랜덤 문제 선택 - 요청한 문제 수보다 DB 문제가 적은 경우")
    void selectRandomQuestionsForRoom_NotEnoughQuestions_Test() {
        // given
        Long roomId = 1L;
        int questionCount = 5;
        
        BalanceQuestion question1 = createBalanceQuestion(1L, "치킨", "피자");
        BalanceQuestion question2 = createBalanceQuestion(2L, "개", "고양이");
        List<BalanceQuestion> allQuestions = new ArrayList<>(List.of(question1, question2)); // 2개만 있음
        
        when(balanceSessionQuestionRepository.existsByRoomId(roomId)).thenReturn(false);
        when(balanceQuestionRepository.findAllQuestions()).thenReturn(allQuestions);

        // when & then
        assertThatThrownBy(() -> balanceQuestionService.selectRandomQuestionsForRoom(roomId, questionCount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("요청한 문제 수보다 DB에 저장된 문제가 적습니다. 요청: 5, 저장: 2");
    }

    @Test
    @DisplayName("현재 문제 조회")
    void getCurrentQuestion_Test() {
        // given
        Long roomId = 1L;
        BalanceSession session = BalanceSession.createInitialSession(roomId, 4, 3);
        session.startQuestionRevealPhase(); // currentQuestionIndex = 0
        
        BalanceSessionQuestion currentQuestion = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        
        when(balanceSessionService.findSessionByRoomId(roomId)).thenReturn(session);
        when(balanceSessionQuestionRepository.findByRoomIdAndDisplayOrder(roomId, 0))
                .thenReturn(Optional.of(currentQuestion));

        // when
        BalanceQuestionResponse response = balanceQuestionService.getCurrentQuestion(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.questionA()).isEqualTo("치킨");
            softly.assertThat(response.questionB()).isEqualTo("피자");
            softly.assertThat(response.currentRound()).isEqualTo(1); // 0 + 1 = 1
            softly.assertThat(response.totalRounds()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("순서로 문제 조회")
    void getQuestionByDisplayOrder_Test() {
        // given
        Long roomId = 1L;
        int order = 1;
        BalanceSessionQuestion expectedQuestion = createBalanceSessionQuestion(2L, roomId, order, "개", "고양이");
        
        when(balanceSessionQuestionRepository.findByRoomIdAndDisplayOrder(roomId, order))
                .thenReturn(Optional.of(expectedQuestion));

        // when
        BalanceSessionQuestion result = balanceQuestionService.getQuestionByDisplayOrder(roomId, order);

        // then
        assertThat(result).isEqualTo(expectedQuestion);
        verify(balanceSessionQuestionRepository).findByRoomIdAndDisplayOrder(roomId, order);
    }

    @Test
    @DisplayName("존재하지 않는 순서로 문제 조회 시 예외")
    void getQuestionByDisplayOrder_NotFound_Test() {
        // given
        Long roomId = 1L;
        int order = 99;
        
        when(balanceSessionQuestionRepository.findByRoomIdAndDisplayOrder(roomId, order))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> balanceQuestionService.getQuestionByDisplayOrder(roomId, order))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 순서의 문제를 찾을 수 없습니다: roomId=1, order=99");
    }

    @Test
    @DisplayName("현재 문제를 순서로 조회")
    void getCurrentQuestionByDisplayOrder_Test() {
        // given
        Long roomId = 1L;
        int order = 2;
        BalanceSessionQuestion expectedQuestion = createBalanceSessionQuestion(3L, roomId, order, "여행", "휴식");
        
        when(balanceSessionQuestionRepository.findByRoomIdAndDisplayOrder(roomId, order))
                .thenReturn(Optional.of(expectedQuestion));

        // when
        BalanceSessionQuestion result = balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, order);

        // then
        assertThat(result).isEqualTo(expectedQuestion);
    }

    @Test
    @DisplayName("방의 모든 문제 조회")
    void getAllQuestionsForRoom_Test() {
        // given
        Long roomId = 1L;
        BalanceSessionQuestion question1 = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        BalanceSessionQuestion question2 = createBalanceSessionQuestion(2L, roomId, 1, "개", "고양이");
        BalanceSessionQuestion question3 = createBalanceSessionQuestion(3L, roomId, 2, "여행", "휴식");
        List<BalanceSessionQuestion> expectedQuestions = List.of(question1, question2, question3);
        
        when(balanceSessionQuestionRepository.findByRoomIdOrderByDisplayOrder(roomId))
                .thenReturn(expectedQuestions);

        // when
        List<BalanceSessionQuestion> result = balanceQuestionService.getAllQuestionsForRoom(roomId);

        // then
        assertThat(result).isEqualTo(expectedQuestions);
        verify(balanceSessionQuestionRepository).findByRoomIdOrderByDisplayOrder(roomId);
    }

    @Test
    @DisplayName("BalanceSessionQuestions 도메인 객체 조회")
    void getBalanceSessionQuestions_Test() {
        // given
        Long roomId = 1L;
        BalanceSessionQuestion question1 = createBalanceSessionQuestion(1L, roomId, 0, "치킨", "피자");
        BalanceSessionQuestion question2 = createBalanceSessionQuestion(2L, roomId, 1, "개", "고양이");
        List<BalanceSessionQuestion> questions = List.of(question1, question2);
        
        when(balanceSessionQuestionRepository.findByRoomIdOrderByDisplayOrder(roomId))
                .thenReturn(questions);

        // when
        BalanceSessionQuestions result = balanceQuestionService.getBalanceSessionQuestions(roomId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(2);
        verify(balanceSessionQuestionRepository).findByRoomIdOrderByDisplayOrder(roomId);
    }

    @Test
    @DisplayName("빈 문제 목록으로 BalanceSessionQuestions 조회 시 예외")
    void getBalanceSessionQuestions_EmptyList_Test() {
        // given
        Long roomId = 1L;
        List<BalanceSessionQuestion> emptyQuestions = List.of();
        
        when(balanceSessionQuestionRepository.findByRoomIdOrderByDisplayOrder(roomId))
                .thenReturn(emptyQuestions);

        // when & then
        assertThatThrownBy(() -> balanceQuestionService.getBalanceSessionQuestions(roomId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("밸런스 게임 문제 목록이 비어있습니다");
    }

    private BalanceQuestion createBalanceQuestion(Long id, String questionA, String questionB) {
        // BalanceQuestion은 protected 생성자를 가지고 있으므로 리플렉션을 이용해서 생성
        // 또는 테스트에서는 mock 객체 사용
        BalanceQuestion question = mock(BalanceQuestion.class);
        when(question.getId()).thenReturn(id);
        when(question.getQuestionA()).thenReturn(questionA);
        when(question.getQuestionB()).thenReturn(questionB);
        return question;
    }

    private BalanceSessionQuestion createBalanceSessionQuestion(Long id, Long roomId, int displayOrder, String questionA, String questionB) {
        return BalanceSessionQuestion.builder()
                .roomId(roomId)
                .balanceQuestionId(id)
                .questionA(questionA)
                .questionB(questionB)
                .displayOrder(displayOrder)
                .build();
    }
} 