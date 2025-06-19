package com.team6.team6.keyword.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KeywordGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String representativeKeyword;

    @Builder
    private KeywordGroup(String representativeKeyword) {
        this.representativeKeyword = representativeKeyword;
    }

    public static KeywordGroup create(String representativeKeyword) {
        return KeywordGroup.builder()
                .representativeKeyword(representativeKeyword)
                .build();
    }
}
