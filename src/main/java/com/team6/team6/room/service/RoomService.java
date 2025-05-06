package com.team6.team6.room.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.domain.RoomExpiryManager;
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

import java.time.Duration;


@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomKeyGenerator roomKeyGenerator;
    private final AnalysisResultStore analysisResultStore;
    private final RoomExpiryManager roomExpiryManager;

    @Retryable(maxAttempts = 3, retryFor = DataIntegrityViolationException.class)
    @Transactional
    public RoomResponse createRoom(RoomCreateServiceRequest request) {
        String roomKey = roomKeyGenerator.generateRoomKey();
        Room room = Room.create(roomKey, request);
        Room savedRoom = roomRepository.save(room);

        // 방 만료 타이머 설정
        scheduleRoomExpiryTimers(roomKey, request.durationMinutes());

        return RoomResponse.from(savedRoom);
    }


    @Recover
    public RoomResponse recoverCreateRoom(Exception e, RoomCreateServiceRequest request) {
        throw new RuntimeException("방 생성에 실패했습니다.", e);
    }

    public RoomResponse getRoom(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 방입니다."));

        if (room.getClosedAt() != null) {
            throw new IllegalStateException("종료된 방입니다.");
        }

        return RoomResponse.from(room);
    }

    @Transactional
    public void closeRoom(String roomKey) {
        Room room = roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 방입니다."));

        if (room.getClosedAt() != null) {
            throw new IllegalStateException("이미 종료된 방입니다.");
        }

        room.closeRoom();
        roomRepository.save(room);

        // 방 관련 모든 타이머 취소
        roomExpiryManager.cancelAllTimers(roomKey);
    }


    /**
     * 방 만료 관련 타이머를 설정합니다.
     *
     * @param roomKey         방 식별 키
     * @param durationMinutes 방 지속 시간 (분)
     */
    private void scheduleRoomExpiryTimers(String roomKey, int durationMinutes) {
        // 방 종료 5분 전 알림을 위한 타이머 설정
        if (durationMinutes > 5) {
            int warningTimeInMinutes = durationMinutes - 5;
            roomExpiryManager.scheduleExpiryWarning(
                    roomKey,
                    Duration.ofMinutes(warningTimeInMinutes)
            );
        }

        // 방 종료 알림을 위한 타이머 설정
        roomExpiryManager.scheduleRoomClosure(
                roomKey,
                Duration.ofMinutes(durationMinutes)
        );
    }
}
