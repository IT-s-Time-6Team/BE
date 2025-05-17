package com.team6.team6.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CharacterType {
    RABBIT("토끼", 1),
    CHICK("병아리", 2),
    PANDA("판다", 3),
    FOX("여우", 4),
    PIG("돼지", 5),
    TURTLE("거북이", 6),
    BEAR("곰", 7),
    UNDEFINED("미지정", 8);

    private final String name;
    private final int order;

    public static CharacterType fromOrder(int order) {
        if (order < 1 || order > 7) {
            return UNDEFINED;
        }

        for (CharacterType character : values()) {
            if (character.getOrder() == order) {
                return character;
            }
        }
        return UNDEFINED;
    }
}