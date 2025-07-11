package com.team6.team6.room.dto;

import com.team6.team6.member.entity.CharacterType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class KeywordRoomResultTest {

    @Test
    void KeywordRoomResult_생성_테스트() {
        // given
        List<String> sharedKeywords = List.of("여행", "음악", "영화");
        Duration totalDuration = Duration.ofHours(1).plusMinutes(30).plusSeconds(45);

        List<MemberKeywordCount> topKeywordContributors = List.of(
                new MemberKeywordCount("사용자1", 10),
                new MemberKeywordCount("사용자2", 10)
        );

        List<MemberKeywordCount> mostSharedKeywordUsers = List.of(
                new MemberKeywordCount("사용자3", 3),
                new MemberKeywordCount("사용자4", 3)
        );

        String requestMemberName = "사용자1";
        CharacterType requestMemberCharacter = CharacterType.RABBIT;

        // when
        KeywordRoomResult result = KeywordRoomResult.of(
                sharedKeywords,
                totalDuration,
                topKeywordContributors,
                mostSharedKeywordUsers,
                requestMemberName,
                requestMemberCharacter
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.sharedKeywords()).isEqualTo(sharedKeywords);
            softly.assertThat(result.totalDuration()).isEqualTo("1시간 30분 45초");
            softly.assertThat(result.topKeywordContributorNames()).containsExactly("사용자1", "사용자2");
            softly.assertThat(result.topKeywordCount()).isEqualTo(10);
            softly.assertThat(result.mostMatchedHobbyUserNames()).containsExactly("사용자3", "사용자4");
            softly.assertThat(result.matchedHobbyCount()).isEqualTo(3);
            softly.assertThat(result.requestMemberName()).isEqualTo("사용자1");
            softly.assertThat(result.requestMemberCharacter()).isEqualTo(CharacterType.RABBIT);
        });
    }

    @Test
    void 시간_형식에_따라_다른_대화_시간_테스트() {
        // given
        List<String> sharedKeywords = List.of("여행");
        List<MemberKeywordCount> contributors = List.of(new MemberKeywordCount("사용자1", 1));
        String requestMemberName = "사용자1";
        CharacterType requestMemberCharacter = CharacterType.RABBIT;

        // when - 초만 있는 경우
        KeywordRoomResult secondsOnly = KeywordRoomResult.of(
                sharedKeywords,
                Duration.ofSeconds(45),
                contributors,
                contributors,
                requestMemberName,
                requestMemberCharacter
        );

        // when - 분과 초만 있는 경우
        KeywordRoomResult minutesAndSeconds = KeywordRoomResult.of(
                sharedKeywords,
                Duration.ofMinutes(25).plusSeconds(30),
                contributors,
                contributors,
                requestMemberName,
                requestMemberCharacter
        );

        // when - 시간, 분, 초가 모두 있는 경우
        KeywordRoomResult hoursMinutesSeconds = KeywordRoomResult.of(
                sharedKeywords,
                Duration.ofHours(2).plusMinutes(15).plusSeconds(10),
                contributors,
                contributors,
                requestMemberName,
                requestMemberCharacter
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(secondsOnly.totalDuration()).isEqualTo("45초");
            softly.assertThat(minutesAndSeconds.totalDuration()).isEqualTo("25분 30초");
            softly.assertThat(hoursMinutesSeconds.totalDuration()).isEqualTo("2시간 15분 10초");
        });
    }

    @Test
    void 비어있는_목록_테스트() {
        // given
        List<String> emptyKeywords = Collections.emptyList();
        Duration duration = Duration.ofMinutes(10);
        List<MemberKeywordCount> emptyContributors = Collections.emptyList();
        String requestMemberName = "사용자1";
        CharacterType requestMemberCharacter = CharacterType.RABBIT;

        // when
        KeywordRoomResult result = KeywordRoomResult.of(
                emptyKeywords,
                duration,
                emptyContributors,
                emptyContributors,
                requestMemberName,
                requestMemberCharacter
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.sharedKeywords()).isEmpty();
            softly.assertThat(result.totalDuration()).isEqualTo("10분 0초");
            softly.assertThat(result.topKeywordContributorNames()).isEmpty();
            softly.assertThat(result.topKeywordCount()).isEqualTo(0);
            softly.assertThat(result.mostMatchedHobbyUserNames()).isEmpty();
            softly.assertThat(result.matchedHobbyCount()).isEqualTo(0);
            softly.assertThat(result.requestMemberName()).isEqualTo("사용자1");
            softly.assertThat(result.requestMemberCharacter()).isEqualTo(CharacterType.RABBIT);
        });
    }
} 