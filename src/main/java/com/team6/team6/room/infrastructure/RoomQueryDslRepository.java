package com.team6.team6.room.infrastructure;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team6.team6.keyword.entity.QKeyword;
import com.team6.team6.room.dto.MemberKeywordCount;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.team6.team6.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class RoomQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<MemberKeywordCount> findMembersWithMostKeywordsInRoom(String roomKey) {
        QKeyword keyword = QKeyword.keyword1;

        // 1. 해당 방에서 가장 많이 생성된 키워드 수를 조회
        Long maxKeywordCount = jpaQueryFactory
                .select(keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(member.room.roomKey.eq(roomKey))
                .groupBy(keyword.memberId)
                .orderBy(keyword.count().desc())
                .fetchFirst();

        // 가장 많이 생성된 키워드가 없는 경우 빈 리스트 반환
        if (maxKeywordCount == null) {
            return Collections.emptyList();
        }

        // 2. 최대 키워드 수만큼 생성한: 모든 멤버 정보 조회
        List<Tuple> results = jpaQueryFactory
                .select(member.nickname, keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(member.room.roomKey.eq(roomKey))
                .groupBy(keyword.memberId)
                .having(keyword.count().eq(maxKeywordCount))
                .fetch();

        // 결과를 DTO로 변환하여 반환
        return results.stream()
                .map(tuple -> new MemberKeywordCount(
                        tuple.get(member.nickname),
                        tuple.get(keyword.count()).intValue()))
                .collect(Collectors.toList());
    }

    public List<MemberKeywordCount> findMembersWithMostSharedKeywordsInRoom(String roomKey, List<String> sharedKeywords) {
        QKeyword keyword = QKeyword.keyword1;

        // 공유 키워드가 없으면 빈 리스트 반환
        if (sharedKeywords == null || sharedKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 각 멤버별로 공유 키워드를 몇 개 가지고 있는지 계산
        // 해당 방에서 공유 키워드들 중 가장 많이 가진 개수를 조회
        Long maxSharedKeywordCount = jpaQueryFactory
                .select(keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(
                        member.room.roomKey.eq(roomKey),
                        keyword.keyword.in(sharedKeywords)
                )
                .groupBy(keyword.memberId)
                .orderBy(keyword.count().desc())
                .fetchFirst();

        // 공유 키워드를 가진 멤버가 없는 경우 빈 리스트 반환
        if (maxSharedKeywordCount == null) {
            return Collections.emptyList();
        }

        // 2. 최대 공유 키워드 수를 가진 모든 멤버 정보 조회
        List<Tuple> results = jpaQueryFactory
                .select(member.nickname, keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(
                        member.room.roomKey.eq(roomKey),
                        keyword.keyword.in(sharedKeywords)
                )
                .groupBy(keyword.memberId)
                .having(keyword.count().eq(maxSharedKeywordCount))
                .fetch();

        // 결과를 DTO로 변환하여 반환
        return results.stream()
                .map(tuple -> new MemberKeywordCount(
                        tuple.get(member.nickname),
                        tuple.get(keyword.count()).intValue()))
                .collect(Collectors.toList());
    }
}
