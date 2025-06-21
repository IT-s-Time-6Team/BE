package com.team6.team6.room.service;

import com.team6.team6.room.dto.MemberKeywordCount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ResultServiceTest {

    @Autowired
    private ResultService resultService;

    @Test
    void 빈_목록_필터링_테스트() throws Exception {
        // given
        List<MemberKeywordCount> emptyList = Collections.emptyList();

        // when - private 메서드를 리플렉션으로 호출
        Method method = ResultService.class.getDeclaredMethod("findMembersWithMaxCount", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<MemberKeywordCount> result = (List<MemberKeywordCount>) method.invoke(resultService, emptyList);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 최대_카운트_멤버_필터링_테스트() throws Exception {
        // given
        MemberKeywordCount max1 = new MemberKeywordCount("user1", 10);
        MemberKeywordCount max2 = new MemberKeywordCount("user2", 10);
        MemberKeywordCount lower1 = new MemberKeywordCount("user3", 5);
        MemberKeywordCount lower2 = new MemberKeywordCount("user4", 3);

        List<MemberKeywordCount> members = Arrays.asList(max1, lower1, max2, lower2);

        // when - private 메서드를 리플렉션으로 호출
        Method method = ResultService.class.getDeclaredMethod("findMembersWithMaxCount", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<MemberKeywordCount> result = (List<MemberKeywordCount>) method.invoke(resultService, members);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(max1, max2);
    }
} 