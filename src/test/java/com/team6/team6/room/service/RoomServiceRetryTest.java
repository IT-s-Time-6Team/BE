package com.team6.team6.room.service;

import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.entity.GameMode;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import com.team6.team6.room.util.RoomKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RoomServiceRetryTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;

    @MockitoBean
    private RoomKeyGenerator roomKeyGenerator;


    @Test
    void 룸키_중복시_자동_재시도_성공() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );

        // 테스트를 위한 중복 키 상황 시뮬레이션
        String duplicateKey = "duplicate-key";
        String uniqueKey = "unique-key";

        // 중복 키를 가진 방을 미리 저장해두기
        Room existingRoom = Room.create(duplicateKey, request);
        roomRepository.save(existingRoom);

        // 첫 번째 호출에서는 중복 키를, 두 번째 호출에서는 유니크 키를 반환하도록 설정
        when(roomKeyGenerator.generateRoomKey())
                .thenReturn(duplicateKey)
                .thenReturn(uniqueKey);

        // when
        RoomResponse response = roomService.createRoom(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.roomKey()).isEqualTo(uniqueKey);

        // generateRoomKey가 두 번 호출되었는지 확인
        verify(roomKeyGenerator, times(2)).generateRoomKey();
    }

    @Test
    void 룸키_중복_3번_연속_발생시_최종_실패() {
        // given
        RoomCreateServiceRequest request = new RoomCreateServiceRequest(
                3, 6, 30, GameMode.NORMAL
        );

        // 테스트를 위한 중복 키 설정
        String duplicateKey = "duplicate-key";

        // 미리 중복 키로 방 생성해두기
        Room existingRoom = Room.create(duplicateKey, request);
        roomRepository.save(existingRoom);

        // 3번 모두 동일한 중복 키를 반환하도록 설정
        when(roomKeyGenerator.generateRoomKey())
                .thenReturn(duplicateKey)
                .thenReturn(duplicateKey)
                .thenReturn(duplicateKey);

        // when & then
        // 3번의 시도 모두 실패하고 @Recover 메서드가 호출되어 RuntimeException이 발생해야 함
        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("방 생성에 실패했습니다");

        // 3번 호출되었는지 확인
        verify(roomKeyGenerator, times(3)).generateRoomKey();
    }

}