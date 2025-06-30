package com.team6.team6.tmi.domain;

public interface TmiMessagePublisher {
    void notifyTmiCollectionProgress(String roomKey, int progress);

    void notifyTmiCollectionCompleted(String roomKey);

    void notifyTmiVotingStarted(String roomKey);

    void notifyTmiRoundCompleted(String roomKey, int round);

    void notifyTmiVotingProgress(String roomKey, int progress);

    void notifyTmiAllVotingCompleted(String roomKey);

    void notifyTmiHintStarted(String roomKey, String remainingTime);

    void notifyTmiHintTimeRemaining(String roomKey, String remainingTime);

    void notifyTmiHintEnded(String roomKey);
}
