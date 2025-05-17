package com.team6.team6.room.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.room.domain.RoomExpiryManager;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RoomServiceCloseTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @MockitoBean
    private RoomExpiryManager roomExpiryManager;

    @MockitoBean
    private RoomNotificationService roomNotificationService;

    @Test
    void 정상_방_종료시_타이머_취소() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        Room room = Room.create("roomKey1", request);
        roomRepository.save(room);

        // when
        roomService.closeRoom("roomKey1");

        // then
        verify(roomExpiryManager).cancelAllTimers("roomKey1");
    }

    @Test
    void 존재하지_않는_방_종료시_예외() {
        // when & then
        assertThatThrownBy(() -> roomService.closeRoom("notfound"))
                .isInstanceOf(NotFoundException.class);
        verify(roomExpiryManager, never()).cancelAllTimers(anyString());
    }

    @Test
    void 이미_종료된_방_종료시_예외() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        Room room = Room.create("roomKey2", request);
        room.closeRoom();
        roomRepository.save(room);

        // when & then
        assertThatThrownBy(() -> roomService.closeRoom("roomKey2"))
                .isInstanceOf(IllegalStateException.class);
        verify(roomExpiryManager, never()).cancelAllTimers(anyString());
    }


    @Test
    void 방_종료시_알림_전송_확인() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );
        Room room = Room.create("roomKey3", request);
        roomRepository.save(room);

        // when
        roomService.closeRoom("roomKey3");

        // then
        verify(roomExpiryManager).cancelAllTimers("roomKey3");
        verify(roomNotificationService).leaderRoomClosedNotification("roomKey3");
    }
}