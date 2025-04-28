package com.team6.team6.room.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RoomServiceTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void 방_생성_성공() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, LocalDateTime.now().plusMinutes(30), GameMode.NORMAL
        );

        // when
        RoomResponse response = roomService.createRoom(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.roomKey()).isNotNull();

        // 실제 DB에서 조회해서 확인
        Room room = roomRepository.findByRoomKey(response.roomKey()).orElseThrow();
        assertThat(room.getRequiredAgreements()).isEqualTo(request.requiredAgreements());
        assertThat(room.getMaxMember()).isEqualTo(request.maxMember());
        assertThat(room.getGameMode()).isEqualTo(request.gameMode());
    }


    @Test
    void 존재하는_방_조회_성공() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, LocalDateTime.now().plusMinutes(30), GameMode.NORMAL
        );
        RoomResponse createdRoom = roomService.createRoom(request);
        String roomKey = createdRoom.roomKey();

        // when
        RoomResponse response = roomService.getRoom(roomKey);

        // then
        assertThat(response).isNotNull();
        assertThat(response.roomKey()).isEqualTo(roomKey);
        assertThat(response.requiredAgreements()).isEqualTo(request.requiredAgreements());
        assertThat(response.maxMember()).isEqualTo(request.maxMember());
        assertThat(response.gameMode()).isEqualTo(request.gameMode());
    }

    @Test
    void 존재하지_않는_방_조회시_예외발생() {
        // given
        String nonExistentRoomKey = "nonexistent";

        // when, then
        assertThatThrownBy(() -> roomService.getRoom(nonExistentRoomKey))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 방입니다");
    }

    @Test
    void 종료된_방_조회시_예외발생() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, LocalDateTime.now().plusMinutes(30), GameMode.NORMAL
        );
        RoomResponse createdRoom = roomService.createRoom(request);
        String roomKey = createdRoom.roomKey();

        // 방 종료
        roomService.closeRoom(roomKey);

        // when, then
        assertThatThrownBy(() -> roomService.getRoom(roomKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("종료된 방입니다");
    }

    @Test
    void 방_종료_성공() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, LocalDateTime.now().plusMinutes(30), GameMode.NORMAL
        );
        RoomResponse createdRoom = roomService.createRoom(request);
        String roomKey = createdRoom.roomKey();

        // when
        roomService.closeRoom(roomKey);

        // then
        Room room = roomRepository.findByRoomKey(roomKey).orElseThrow();
        assertThat(room.getClosedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_방_종료시_예외발생() {
        // given
        String nonExistentRoomKey = "nonexistent";

        // when, then
        assertThatThrownBy(() -> roomService.closeRoom(nonExistentRoomKey))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 방입니다");
    }

    @Test
    void 이미_종료된_방_다시_종료시_예외발생() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, LocalDateTime.now().plusMinutes(30), GameMode.NORMAL
        );
        RoomResponse createdRoom = roomService.createRoom(request);
        String roomKey = createdRoom.roomKey();

        // 방 종료
        roomService.closeRoom(roomKey);

        // when, then
        assertThatThrownBy(() -> roomService.closeRoom(roomKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 종료된 방입니다");
    }
}