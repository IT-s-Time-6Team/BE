package com.team6.team6.room.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameMode {
    NORMAL("일반");

    private final String description;

}