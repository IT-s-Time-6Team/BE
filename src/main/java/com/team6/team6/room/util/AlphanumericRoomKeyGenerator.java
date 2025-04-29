package com.team6.team6.room.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AlphanumericRoomKeyGenerator implements RoomKeyGenerator {

    private static final String ALPHA_NUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int KEY_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateRoomKey() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = random.nextInt(ALPHA_NUMERIC.length());
            builder.append(ALPHA_NUMERIC.charAt(index));
        }

        return builder.toString();
    }
}