package com.team6.team6.room.service;


import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import com.team6.team6.room.dto.RoomResponse;
import com.team6.team6.room.dto.RoomResult;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomKeyGenerator roomKeyGenerator;
    private final AnalysisResultStore analysisResultStore;

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

    public RoomResult getRoomResult(String roomKey) {
        Room room = findRoomByKey(roomKey);
        if (room.getClosedAt() == null) {
            throw new IllegalStateException("아직 종료되지 않은 방입니다.");
        }

        // 1. 총 대화 시간 계산
        Duration totalDuration = Duration.between(room.getCreatedAt(), room.getClosedAt());

        // 2. 가장 많은 키워드를 생성한 멤버와 개수 찾기
        List<MemberKeywordCount> topKeywordContributors = findMembersWithMaxCount(roomRepository.findAllMemberKeywordCountsInRoom(roomKey));

        // 3. 공통 키워드 목록 가져오기
        List<String> referenceNames = analysisResultStore.findReferenceNamesByRoomId(room.getId(), room.getRequiredAgreements());
        List<String> sharedKeywords = analysisResultStore.findSharedKeywordsByRoomId(room.getId(), room.getRequiredAgreements());

        // 4. 공감 키워드가 가장 많은 멤버와 개수 찾기
        List<MemberKeywordCount> mostSharedKeywordUsers =
                findMembersWithMaxCount(roomRepository.findAllMemberSharedKeywordCountsInRoom(roomKey, sharedKeywords));

        return RoomResult.of(
                referenceNames,
                totalDuration,
                topKeywordContributors,
                mostSharedKeywordUsers
        );
    }

    private Room findRoomByKey(String roomKey) {
        return roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 방입니다."));
    }

    public List<MemberKeywordCount> findMembersWithMaxCount(List<MemberKeywordCount> memberKeywordCounts) {
        if (memberKeywordCounts.isEmpty()) {
            return Collections.emptyList();
        }

        int maxCount = memberKeywordCounts.stream()
                .mapToInt(MemberKeywordCount::keywordCount)
                .max()
                .orElse(0);

        return memberKeywordCounts.stream()
                .filter(count -> count.keywordCount() == maxCount)
                .collect(Collectors.toList());
    }
}
