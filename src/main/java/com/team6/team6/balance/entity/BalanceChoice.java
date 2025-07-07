package com.team6.team6.balance.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 밸런스 게임의 선택지를 나타내는 열거형
 */
@Getter
@RequiredArgsConstructor
public enum BalanceChoice {
    A("A"),
    B("B");

    private final String value;
} 