package com.team6.team6.balance.domain;

/**
 * 투표 처리 후 결과 상태를 나타내는 열거형
 */
public enum VoteStatus {
    IN_PROGRESS,         // 투표 진행 중
    ROUND_COMPLETED,     // 현재 라운드 투표 완료, 다음 문제로 이동
    ALL_COMPLETED        // 모든 문제 투표 완료, 게임 종료
} 