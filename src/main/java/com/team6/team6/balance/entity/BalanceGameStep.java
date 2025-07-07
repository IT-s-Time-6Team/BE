package com.team6.team6.balance.entity;

/**
 * 밸런스 게임의 단계를 나타내는 열거형
 */
public enum BalanceGameStep {
    WAITING_FOR_MEMBERS, // 멤버 입장 대기 단계
    QUESTION_REVEAL,     // 문제 공개 단계 (30초)
    DISCUSSION,          // 토론 단계 (5분)  
    VOTING,             // 투표 단계
    RESULT_VIEW,        // 결과 확인 단계
    COMPLETED           // 게임 완료
} 