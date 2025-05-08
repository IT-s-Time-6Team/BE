package com.team6.team6.room.infrastructure;

import com.team6.team6.global.TestQueryDslConfig;
import com.team6.team6.keyword.domain.repository.KeywordRepository;
import com.team6.team6.keyword.entity.Keyword;
import com.team6.team6.member.domain.MemberRepository;
import com.team6.team6.member.entity.Member;
import com.team6.team6.room.dto.MemberKeywordCount;
import com.team6.team6.room.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DataJpaTest
@Import(TestQueryDslConfig.class)
@ActiveProfiles("test")
class RoomQueryDslRepositoryTest {

    @Autowired
    private RoomQueryDslRepository roomQueryDslRepository;

    @Autowired
    private RoomJpaRepository roomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private KeywordRepository keywordRepository;

    private String roomKey;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        // 1. 방 생성
        Room room = Room.builder()
                .roomKey("test-room-key")
                .build();
        roomRepository.save(room);
        roomKey = room.getRoomKey();

        // 2. 멤버 생성
        member1 = Member.builder()
                .nickname("사용자1")
                .room(room)
                .build();
        member2 = Member.builder()
                .nickname("사용자2")
                .room(room)
                .build();
        member3 = Member.builder()
                .nickname("사용자3")
                .room(room)
                .build();
        memberRepository.saveAll(Arrays.asList(member1, member2, member3));

        // 3. 키워드 저장 (entityManager 직접 사용)
        // member1: 3개 키워드 (Java, Python, JavaScript)
        saveKeyword(member1.getId(), "Java");
        saveKeyword(member1.getId(), "Python");
        saveKeyword(member1.getId(), "JavaScript");

        // member2: 3개 키워드 (Java, Spring, Kotlin)
        saveKeyword(member2.getId(), "Java");
        saveKeyword(member2.getId(), "Spring");
        saveKeyword(member2.getId(), "Kotlin");

        // member3: 2개 키워드 (Java, React)
        saveKeyword(member3.getId(), "Java");
        saveKeyword(member3.getId(), "React");
    }

    private void saveKeyword(Long memberId, String keywordName) {
        Keyword keyword = Keyword.builder()
                .memberId(memberId)
                .keyword(keywordName)
                .build();
        keywordRepository.save(keyword);
    }

    @Test
    void 방에서_모든_키워드_생성_멤버_테스트() {
        // when
        List<MemberKeywordCount> result = roomQueryDslRepository.findAllMemberKeywordCountsInRoom(roomKey);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.stream().map(MemberKeywordCount::memberName).toList())
                    .containsExactlyInAnyOrder("사용자1", "사용자2", "사용자3");
            Map<String, Integer> countByMember = result.stream()
                    .collect(Collectors.toMap(
                            MemberKeywordCount::memberName,
                            MemberKeywordCount::keywordCount
                    ));

            softly.assertThat(countByMember.get("사용자1")).isEqualTo(3);
            softly.assertThat(countByMember.get("사용자2")).isEqualTo(3);
            softly.assertThat(countByMember.get("사용자3")).isEqualTo(2);
        });
    }

    @Test
    void 방에_멤버가_없을_때_테스트() {
        // given
        String emptyRoomKey = "empty-room";
        Room emptyRoom = Room.builder()
                .roomKey(emptyRoomKey)
                .build();
        roomRepository.save(emptyRoom);

        // when
        List<MemberKeywordCount> result = roomQueryDslRepository.findAllMemberKeywordCountsInRoom(emptyRoomKey);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isEmpty();
        });
    }

    @Test
    void 방에서_모든_공유_키워드_멤버_조회_테스트() {
        // given
        List<String> sharedKeywords = Arrays.asList("Java", "Python", "Spring");

        // when
        List<MemberKeywordCount> result = roomQueryDslRepository.findAllMemberSharedKeywordCountsInRoom(roomKey, sharedKeywords);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.stream().map(MemberKeywordCount::memberName).toList())
                    .containsExactlyInAnyOrder("사용자1", "사용자2", "사용자3");
            Map<String, Integer> countByMember = result.stream()
                    .collect(Collectors.toMap(
                            MemberKeywordCount::memberName,
                            MemberKeywordCount::keywordCount
                    ));

            softly.assertThat(countByMember.get("사용자1")).isEqualTo(2); // Java, Python
            softly.assertThat(countByMember.get("사용자2")).isEqualTo(2); // Java, Spring
            softly.assertThat(countByMember.get("사용자3")).isEqualTo(1); // Java
        });
    }

    @Test
    void 공유_키워드_목록이_비어있을_때_테스트() {
        // when
        List<MemberKeywordCount> result = roomQueryDslRepository.findAllMemberSharedKeywordCountsInRoom(roomKey, List.of());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isEmpty();
        });
    }
}