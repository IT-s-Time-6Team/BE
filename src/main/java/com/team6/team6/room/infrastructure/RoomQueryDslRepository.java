package com.team6.team6.room.infrastructure;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team6.team6.keyword.entity.QKeyword;
import com.team6.team6.room.dto.MemberKeywordCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.team6.team6.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class RoomQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 방에 있는 모든 멤버의 키워드 카운트를 조회
    public List<MemberKeywordCount> findAllMemberKeywordCountsInRoom(String roomKey) {
        QKeyword keyword = QKeyword.keyword1;

        List<Tuple> results = jpaQueryFactory
                .select(member.nickname, keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(member.room.roomKey.eq(roomKey))
                .groupBy(keyword.memberId)
                .orderBy(keyword.count().desc())
                .fetch();

        return results.stream()
                .map(tuple -> new MemberKeywordCount(
                        tuple.get(member.nickname),
                        tuple.get(keyword.count()).intValue()))
                .collect(Collectors.toList());
    }

    // 방에 있는 모든 멤버의 공유 키워드 카운트를 조회
    public List<MemberKeywordCount> findAllMemberSharedKeywordCountsInRoom(String roomKey, List<String> sharedKeywords) {
        QKeyword keyword = QKeyword.keyword1;

        if (sharedKeywords == null || sharedKeywords.isEmpty()) {
            return Collections.emptyList();
        }

        List<Tuple> results = jpaQueryFactory
                .select(member.nickname, keyword.count())
                .from(keyword)
                .join(member).on(keyword.memberId.eq(member.id))
                .where(
                        member.room.roomKey.eq(roomKey),
                        keyword.keyword.in(sharedKeywords)
                )
                .groupBy(keyword.memberId)
                .orderBy(keyword.count().desc())
                .fetch();

        return results.stream()
                .map(tuple -> new MemberKeywordCount(
                        tuple.get(member.nickname),
                        tuple.get(keyword.count()).intValue()))
                .collect(Collectors.toList());
    }
}