package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.KeywordManager;
import com.team6.team6.keyword.domain.repository.MemberRegistryRepository;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.ChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketSubscribeServiceTest {

    private final String roomKey = "test-room";
    private final String nickname = "test-user";
    private final Long roomId = 1L;
    private final Long memberId = 1L;
    @Mock
    private MemberRegistryRepository memberRegistryRepository;
    @Mock
    private KeywordManager keywordManager;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private KeywordService keywordService;
    @InjectMocks
    private WebSocketSubscribeService webSocketSubscribeService;

    @BeforeEach
    void setUp() {
        // Common setup for tests
    }

    @Test
    void 새로운_사용자_입장_처리_테스트() {
        // Given
        when(memberRegistryRepository.isUserInRoom(roomKey, nickname)).thenReturn(false);
        when(memberRegistryRepository.getOnlineUserCount(roomKey)).thenReturn(1);

        // When
        ChatMessage result = webSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // Then
        verify(memberRegistryRepository).registerUserInRoom(roomKey, nickname);
        verify(memberRegistryRepository, never()).setUserOnline(roomKey, nickname);
        verify(keywordService, never()).getUserKeywords(roomId, memberId);

        assertSoftly(softly -> {
            softly.assertThat(result.type()).isEqualTo(ChatMessage.MessageType.ENTER);
            softly.assertThat(result.nickname()).isEqualTo(nickname);
            softly.assertThat(((ChatMessage.UserCountData) result.data()).userCount()).isEqualTo(1);
            softly.assertThat(result.data()).isNotNull();
        });
    }

    @Test
    void 재입장_사용자_처리_테스트() {
        // Given
        when(memberRegistryRepository.isUserInRoom(roomKey, nickname)).thenReturn(true);
        when(memberRegistryRepository.getOnlineUserCount(roomKey)).thenReturn(2);

        Keyword keyword = Keyword.builder()
                .keyword("test-keyword")
                .roomId(roomId)
                .memberId(memberId)
                .build();
        when(keywordService.getUserKeywords(roomId, memberId)).thenReturn(List.of(keyword));

        // When
        ChatMessage result = webSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // Then
        verify(memberRegistryRepository).setUserOnline(roomKey, nickname);
        verify(memberRegistryRepository, never()).registerUserInRoom(roomKey, nickname);
        verify(keywordService).getUserKeywords(roomId, memberId);

        assertInstanceOf(ChatMessage.ReenterData.class, result.data());
        ChatMessage.ReenterData reenterData = (ChatMessage.ReenterData) result.data();

        assertSoftly(softly -> {
            softly.assertThat(result.type()).isEqualTo(ChatMessage.MessageType.REENTER);
            softly.assertThat(result.nickname()).isEqualTo(nickname);
            softly.assertThat(reenterData.userCount()).isEqualTo(2);
            softly.assertThat(reenterData.keywords()).isNotNull();
            softly.assertThat(reenterData.keywords()).hasSize(1);
            softly.assertThat(reenterData.keywords().get(0)).isEqualTo("test-keyword");
        });
    }

    @Test
    void 키워드_분석_결과_발행_테스트_결과_있음() {
        // Given
        AnalysisResult result = mock(AnalysisResult.class);
        when(keywordManager.getAnalysisResult(roomId)).thenReturn(List.of(result));

        // When
        webSocketSubscribeService.publishAnalysisResults(roomKey, roomId);

        // Then
        verify(messagePublisher).publishKeywordAnalysisResult(roomKey, List.of(result));
    }

    @Test
    void 키워드_분석_결과_발행_테스트_결과_없음() {
        // Given
        when(keywordManager.getAnalysisResult(roomId)).thenReturn(Collections.emptyList());

        // When
        webSocketSubscribeService.publishAnalysisResults(roomKey, roomId);

        // Then
        verify(messagePublisher, never()).publishKeywordAnalysisResult(eq(roomKey), any());
    }
}