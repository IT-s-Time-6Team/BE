package com.team6.team6.websocket.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomMemberStateManagerTest {

    private final String roomKey = "test-room";
    private final String nickname = "test-user";
    @Mock
    private RoomMemberStateRepository repository;
    @InjectMocks
    private RoomMemberStateManager manager;

    @BeforeEach
    void setUp() {
        // Common setup for tests
    }

    @Test
    void 첫_연결시_사용자를_등록한다() {
        // Given
        when(repository.isUserInRoom(roomKey, nickname)).thenReturn(false);

        // When
        manager.handleUserOnlineStatus(roomKey, nickname, true);

        // Then
        verify(repository).registerUserInRoom(roomKey, nickname);
        verify(repository, never()).setUserOnline(roomKey, nickname);
        verify(repository, never()).setFirstConnection(roomKey, nickname, false);
    }

    @Test
    void 재연결시_온라인_상태로_변경하고_첫_연결을_false로_설정한다() {
        // Given
        when(repository.isUserInRoom(roomKey, nickname)).thenReturn(true);

        // When
        manager.handleUserOnlineStatus(roomKey, nickname, true);

        // Then
        verify(repository).setUserOnline(roomKey, nickname);
        verify(repository).setFirstConnection(roomKey, nickname, false);
        verify(repository, never()).registerUserInRoom(roomKey, nickname);
    }

    @Test
    void 연결_해제시_오프라인_상태로_변경하고_첫_연결을_true로_설정한다() {
        // Given
        // When
        manager.handleUserOnlineStatus(roomKey, nickname, false);

        // Then
        verify(repository).setUserOffline(roomKey, nickname);
        verify(repository).setFirstConnection(roomKey, nickname, true);
    }

    @Test
    void 첫_연결_여부를_확인한다() {
        // Given
        when(repository.isFirstConnection(roomKey, nickname)).thenReturn(true);

        // When
        boolean result = manager.isFirstConnection(roomKey, nickname);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
        });
        verify(repository).isFirstConnection(roomKey, nickname);
    }

    @Test
    void 구독_완료_표시를_한다() {
        // When
        manager.markNotFirstConnection(roomKey, nickname);

        // Then
        verify(repository).setFirstConnection(roomKey, nickname, false);
    }

    @Test
    void 온라인_사용자_수를_조회한다() {
        // Given
        when(repository.getOnlineUserCount(roomKey)).thenReturn(3);

        // When
        int result = manager.getOnlineUserCount(roomKey);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isEqualTo(3);
        });
        verify(repository).getOnlineUserCount(roomKey);
    }

    @Test
    void 사용자가_방에_있는지_확인한다() {
        // Given
        when(repository.isUserInRoom(roomKey, nickname)).thenReturn(true);

        // When
        boolean result = manager.isUserInRoom(roomKey, nickname);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
        });
        verify(repository).isUserInRoom(roomKey, nickname);
    }

    @Test
    void 사용자가_온라인인지_확인한다() {
        // Given
        when(repository.isUserOnline(roomKey, nickname)).thenReturn(true);

        // When
        boolean result = manager.isUserOnline(roomKey, nickname);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isTrue();
        });
        verify(repository).isUserOnline(roomKey, nickname);
    }

    @Test
    void 사용자를_방에_등록한다() {
        // When
        manager.registerUserInRoom(roomKey, nickname);

        // Then
        verify(repository).registerUserInRoom(roomKey, nickname);
    }

    @Test
    void 사용자를_온라인_상태로_설정한다() {
        // When
        manager.setUserOnline(roomKey, nickname);

        // Then
        verify(repository).setUserOnline(roomKey, nickname);
    }

    @Test
    void 사용자를_오프라인_상태로_설정한다() {
        // When
        manager.setUserOffline(roomKey, nickname);

        // Then
        verify(repository).setUserOffline(roomKey, nickname);
    }

    @Test
    void 방_멤버_목록을_조회한다() {
        // Given
        Map<String, Boolean> expectedMembers = Map.of("user1", true, "user2", false);
        when(repository.getRoomMembers(roomKey)).thenReturn(expectedMembers);

        // When
        Map<String, Boolean> result = manager.getRoomMembers(roomKey);

        // Then
        assertSoftly(softly -> {
            softly.assertThat(result).isEqualTo(expectedMembers);
        });
        verify(repository).getRoomMembers(roomKey);
    }

    @Test
    void 사용자를_완전히_제거한다() {
        // When
        manager.removeUser(roomKey, nickname);

        // Then
        verify(repository).removeUser(roomKey, nickname);
    }
} 