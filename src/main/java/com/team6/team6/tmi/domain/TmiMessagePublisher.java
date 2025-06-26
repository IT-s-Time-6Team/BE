package com.team6.team6.tmi.domain;

public interface TmiMessagePublisher {
    void publishTmiCollectionProgress(String roomKey, int progress);
    void publishTmiCollectionCompleted(String roomKey);
}
