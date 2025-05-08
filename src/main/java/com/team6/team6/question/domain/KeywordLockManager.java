package com.team6.team6.question.domain;

public interface KeywordLockManager {

    /**
     * 주어진 키워드에 대해 잠금을 시도한다.
     * 잠금에 성공한 경우 true, 이미 잠겨있으면 false
     */
    boolean tryLock(String keyword);

    /**
     * 주어진 키워드에 대한 잠금을 해제한다.
     */
    void unlock(String keyword);
}

