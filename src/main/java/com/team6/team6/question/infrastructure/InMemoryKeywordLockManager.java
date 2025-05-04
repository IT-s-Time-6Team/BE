package com.team6.team6.question.infrastructure;

import com.team6.team6.question.domain.KeywordLockManager;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryKeywordLockManager implements KeywordLockManager {

    private final ConcurrentMap<String, Boolean> lockMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String keyword) {
        return lockMap.putIfAbsent(keyword, true) == null;
    }

    @Override
    public void unlock(String keyword) {
        lockMap.remove(keyword);
    }
}
