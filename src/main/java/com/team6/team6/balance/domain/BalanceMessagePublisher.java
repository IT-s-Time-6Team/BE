package com.team6.team6.balance.domain;

public interface BalanceMessagePublisher {
    
    // 멤버 입장 대기 단계
    void notifyBalanceAllMembersJoined(String roomKey);
    
    // 문제 공개 단계
    void notifyBalanceQuestionStarted(String roomKey, String remainingTime);
    void notifyBalanceQuestionTimeRemaining(String roomKey, String remainingTime);
    void notifyBalanceQuestionEnded(String roomKey);
    
    // 토론 단계
    void notifyBalanceDiscussionStarted(String roomKey, String remainingTime);
    void notifyBalanceDiscussionTimeRemaining(String roomKey, String remainingTime);
    void notifyBalanceDiscussionEnded(String roomKey);
    void notifyBalanceDiscussionSkipped(String roomKey);
    
    // 투표 단계
    void notifyBalanceVotingStarted(String roomKey);
    void notifyBalanceVotingProgress(String roomKey, int progress);
    
    // 라운드 완료 및 게임 완료
    void notifyBalanceRoundCompleted(String roomKey, int round);
    void notifyBalanceGameCompleted(String roomKey);
    
    // 결과 확인 단계
    void notifyBalanceGameReady(String roomKey, int currentCount, int totalCount);
} 