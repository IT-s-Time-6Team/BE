package com.team6.team6.tmi.domain;

/**
 * 투표 처리 후 결과 상태를 나타내는 열거형
 */
public enum VoteStatus {
    IN_PROGRESS,      // 투표 진행 중
    ROUND_COMPLETED,  // 현재 라운드 투표 완료, 다음 TMI로 이동
    ALL_COMPLETED     // 모든 TMI 투표 완료, 게임 종료
}
