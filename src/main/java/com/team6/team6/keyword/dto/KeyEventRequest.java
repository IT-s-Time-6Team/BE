package com.team6.team6.keyword.dto;

public record KeyEventRequest(String key) {
    public static KeyEventRequest of(String key) {
        return new KeyEventRequest(key);
    }
}