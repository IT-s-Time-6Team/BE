package com.team6.team6.room.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlphanumericRoomKeyGeneratorTest {

    private final AlphanumericRoomKeyGenerator generator = new AlphanumericRoomKeyGenerator();

    @Test
    void 생성된_방_키의_길이가_6자리인지_확인한다() {
        // when
        String roomKey = generator.generateRoomKey();

        // then
        assertEquals(6, roomKey.length());
    }

    @Test
    void 생성된_방_키가_영소문자와_숫자로만_구성되어있는지_확인한다() {
        // when
        String roomKey = generator.generateRoomKey();

        // then
        assertTrue(Pattern.matches("^[a-z0-9]+$", roomKey));
    }


}