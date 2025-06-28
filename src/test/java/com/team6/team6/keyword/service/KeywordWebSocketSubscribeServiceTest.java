package com.team6.team6.keyword.service;

import com.team6.team6.common.messaging.publisher.MessagePublisher;
import com.team6.team6.keyword.domain.RoomKeywordManager;
import com.team6.team6.keyword.dto.AnalysisResult;
import com.team6.team6.keyword.dto.KeywordChatMessage;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.entity.Member;
import com.team6.team6.room.entity.Room;
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
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks
    private KeywordWebSocketSubscribeService keywordWebSocketSubscribeService;

    private Member testMember;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 테스트용 Room 객체 생성
        testRoom = Room.builder()
                .roomKey(roomKey)
                .build();

        // 테스트용 Member 객체 생성
        testMember = Member.builder()
                .nickname(nickname)
                .character(CharacterType.RABBIT)
                .isLeader(true)
                .room(testRoom)
                .build();
    }

    @Test
    void 첫_연결_사용자_입장_처리_테스트() {
        // Given
        when(roomMemberStateManager.isFirstConnection(roomKey, nickname)).thenReturn(true);
        when(roomMemberStateManager.getOnlineUserCount(roomKey)).thenReturn(1);
        when(memberRepository.findByRoomId(roomId)).thenReturn(List.of(testMember));

        // When
        ChatMessage result = keywordWebSocketSubscribeService.handleUserSubscription(roomKey, nickname, roomId, memberId);

        // Then
        verify(keywordService, never()).getUserKeywords(roomId, memberId);
        verify(memberRepository).findByRoomId(roomId);

        assertInstanceOf(KeywordChatMessage.UserCountData.class, result.getData());
        KeywordChatMessage.UserCountData userData = (KeywordChatMessage.UserCountData) result.getData();

        assertSoftly(softly -> {
            softly.assertThat(result.getType()).isEqualTo("ENTER");
            softly.assertThat(result.getNickname()).isEqualTo(nickname);
            softly.assertThat(userData.userCount()).isEqualTo(1);
            softly.assertThat(userData.roomMembers()).isNotNull();
            softly.assertThat(userData.roomMembers()).hasSize(1);
            softly.assertThat(userData.roomMembers().get(0).nickname()).isEqualTo(nickname);
            softly.assertThat(userData.roomMembers().get(0).character()).isEqualTo(CharacterType.RABBIT);
            softly.assertThat(userData.roomMembers().get(0).isLeader()).isTrue();
        });
    }

    @Test
    void 재연결_사용자_처리_테스트() {
        // Given
        when(roomMemberStateManager.isFirstConnection(roomKey, nickname)).thenReturn(false);
        when(roomMemberStateManager.getOnlineUserCount(roomKey)).thenReturn(2);
        when(memberRepository.findByRoomId(roomId)).thenReturn(List.of(testMember));

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
        verify(memberRepository).findByRoomId(roomId);

        assertInstanceOf(KeywordChatMessage.ReenterData.class, result.getData());
        KeywordChatMessage.ReenterData reenterData = (KeywordChatMessage.ReenterData) result.getData();

        assertSoftly(softly -> {
            softly.assertThat(result.getType()).isEqualTo("REENTER");
            softly.assertThat(result.getNickname()).isEqualTo(nickname);
            softly.assertThat(reenterData.userCount()).isEqualTo(2);
            softly.assertThat(reenterData.keywords()).isNotNull();
            softly.assertThat(reenterData.keywords()).hasSize(1);
            softly.assertThat(reenterData.keywords().get(0)).isEqualTo("test-keyword");
            softly.assertThat(reenterData.roomMembers()).isNotNull();
            softly.assertThat(reenterData.roomMembers()).hasSize(1);
            softly.assertThat(reenterData.roomMembers().get(0).nickname()).isEqualTo(nickname);
            softly.assertThat(reenterData.roomMembers().get(0).character()).isEqualTo(CharacterType.RABBIT);
            softly.assertThat(reenterData.roomMembers().get(0).isLeader()).isTrue();
        });
    }

    @Test
    void 사용자_연결_해제_처리_테스트() {
        // Given
        when(roomMemberStateManager.getOnlineUserCount(roomKey)).thenReturn(0);
        when(memberRepository.findByRoomId(roomId)).thenReturn(Collections.emptyList());

        // When
        ChatMessage result = keywordWebSocketSubscribeService.handleUserDisconnection(roomKey, nickname, roomId);

        // Then
        verify(memberRepository).findByRoomId(roomId);

        assertInstanceOf(KeywordChatMessage.UserCountData.class, result.getData());
        KeywordChatMessage.UserCountData userData = (KeywordChatMessage.UserCountData) result.getData();

        assertSoftly(softly -> {
            softly.assertThat(result.getType()).isEqualTo("LEAVE");
            softly.assertThat(result.getNickname()).isEqualTo(nickname);
            softly.assertThat(userData.userCount()).isEqualTo(0);
            softly.assertThat(userData.roomMembers()).isNotNull();
            softly.assertThat(userData.roomMembers()).isEmpty();
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
