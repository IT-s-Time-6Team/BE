package com.team6.team6.room.service;

import com.team6.team6.global.error.exception.NotFoundException;
import com.team6.team6.keyword.domain.AnalysisResultStore;
import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.room.dto.KeywordRoomResult;
import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.entity.Room;
import com.team6.team6.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ResultService {

    private static final String ANONYMOUS_MEMBER = "anonymous";

    private final RoomRepository roomRepository;
    private final AnalysisResultStore analysisResultStore;

    public KeywordRoomResult getKeywordResult(String roomKey) {
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

        // 5. 요청한 멤버 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requestMemberName = ANONYMOUS_MEMBER;
        CharacterType requestMemberCharacter = null;

        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            requestMemberName = userPrincipal.getNickname();
            requestMemberCharacter = userPrincipal.getCharacter();
        }

        return KeywordRoomResult.of(
                referenceNames,
                totalDuration,
                topKeywordContributors,
                mostSharedKeywordUsers,
                requestMemberName,
                requestMemberCharacter
        );
    }

    private Room findRoomByKey(String roomKey) {
        return roomRepository.findByRoomKey(roomKey)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 방입니다."));
    }

    private List<MemberKeywordCount> findMembersWithMaxCount(List<MemberKeywordCount> memberKeywordCounts) {
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