package com.team6.team6.room.service;


import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import com.team6.team6.room.util.RoomKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomKeyGenerator roomKeyGenerator;


    @Retryable(maxAttempts = 3, retryFor = DataIntegrityViolationException.class)
    @Transactional
    public RoomResponse createRoom(RoomCreateServiceRequest request) {

        String roomKey = roomKeyGenerator.generateRoomKey();
        Room room = Room.create(roomKey, request);
        Room savedRoom = roomRepository.save(room);

        return RoomResponse.from(savedRoom);
    }

    @Recover
    public RoomResponse recoverCreateRoom(Exception e, RoomCreateServiceRequest request) {
        throw new RuntimeException("방 생성에 실패했습니다.", e);
    }

    public RoomResponse getRoom(String roomKey) {
        Room room = findRoomByKey(roomKey);

        if (room.getClosedAt() != null) {
            throw new IllegalStateException("종료된 방입니다.");
        }

        return RoomResponse.from(room);
    }

    @Transactional
    public void closeRoom(String roomKey) {
        Room room = findRoomByKey(roomKey);

        if (room.getClosedAt() != null) {
            throw new IllegalStateException("이미 종료된 방입니다.");
        }

        room.closeRoom();
        roomRepository.save(room);
    }

    private Room findRoomByKey(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 방입니다."));
        return room;
    }
}
