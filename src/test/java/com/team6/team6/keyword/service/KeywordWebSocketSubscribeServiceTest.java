package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.websocket.domain.RoomMemberStateManager;
import com.team6.team6.websocket.dto.ChatMessage;
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
class KeywordWebSocketSubscribeServiceTest {

    private final String roomKey = "test-room";
    private final String nickname = "test-user";
    private final Long roomId = 1L;
    private final Long memberId = 1L;
    @Mock
    private RoomMemberStateManager roomMemberStateManager;
    @Mock
    private RoomKeywordManager roomKeywordManager;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private KeywordService keywordService;
    @InjectMocks
    private KeywordWebSocketSubscribeService keywordWebSocketSubscribeService;

    @BeforeEach
    void setUp() {
        // Common setup for tests
    }

    @Test
    void 첫_연결_사용자_입장_처리_테스트() {
        // Given
        when(roomMemberStateManager.isFirstConnection(roomKey, nickname)).thenReturn(true);
        when(roomMemberStateManager.getOnlineUserCount(roomKey)).thenReturn(1);

        // When
        ChatMessage result = keywordWebSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // Then
        verify(keywordService, never()).getUserKeywords(roomId, memberId);

        assertSoftly(softly -> {
            softly.assertThat(result.getType()).isEqualTo("ENTER");
            softly.assertThat(result.getNickname()).isEqualTo(nickname);
            softly.assertThat(((KeywordChatMessage.UserCountData) result.getData()).userCount()).isEqualTo(1);
            softly.assertThat(result.getData()).isNotNull();
        });
    }

    @Test
    void 재연결_사용자_처리_테스트() {
        // Given
        when(roomMemberStateManager.isFirstConnection(roomKey, nickname)).thenReturn(false);
        when(roomMemberStateManager.getOnlineUserCount(roomKey)).thenReturn(2);

        Keyword keyword = Keyword.builder()
                .keyword("test-keyword")
                .roomId(roomId)
                .memberId(memberId)
                .build();
        when(keywordService.getUserKeywords(roomId, memberId)).thenReturn(List.of(keyword));

        // When
        ChatMessage result = keywordWebSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // Then
        verify(keywordService).getUserKeywords(roomId, memberId);

        assertInstanceOf(KeywordChatMessage.ReenterData.class, result.getData());
        KeywordChatMessage.ReenterData reenterData = (KeywordChatMessage.ReenterData) result.getData();

        assertSoftly(softly -> {
            softly.assertThat(result.getType()).isEqualTo("REENTER");
            softly.assertThat(result.getNickname()).isEqualTo(nickname);
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
        when(roomKeywordManager.getAnalysisResult(roomId)).thenReturn(List.of(result));

        // When
        keywordWebSocketSubscribeService.publishAnalysisResults(roomKey, roomId);

        // Then
        verify(messagePublisher).publishKeywordAnalysisResult(roomKey, List.of(result));
    }

    @Test
    void 키워드_분석_결과_발행_테스트_결과_없음() {
        // Given
        when(roomKeywordManager.getAnalysisResult(roomId)).thenReturn(Collections.emptyList());

        // When
        keywordWebSocketSubscribeService.publishAnalysisResults(roomKey, roomId);

        // Then
        verify(messagePublisher, never()).publishKeywordAnalysisResult(eq(roomKey), any());
    }
}
