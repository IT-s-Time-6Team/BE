package com.team6.team6.room.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RoomQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;


}
