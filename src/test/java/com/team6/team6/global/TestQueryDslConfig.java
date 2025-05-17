package com.team6.team6.global;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team6.team6.room.infrastructure.RoomQueryDslRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestQueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public RoomQueryDslRepository roomQueryDslRepository() {
        return new RoomQueryDslRepository(jpaQueryFactory());
    }
}
