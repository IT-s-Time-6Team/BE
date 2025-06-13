package com.team6.team6.question.infrastructure;

import com.team6.team6.question.domain.KeywordLockManager;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Profile("!test")
public class RedisKeywordLockManager implements KeywordLockManager {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "keyword_lock:";
    private static final long DEFAULT_LOCK_TIMEOUT = 10; // 10초 후 자동 해제

    public RedisKeywordLockManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(String keyword) {
        String lockKey = LOCK_PREFIX + keyword;
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, "locked", DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)
        );
    }

    @Override
    public void unlock(String keyword) {
        String lockKey = LOCK_PREFIX + keyword;
        redisTemplate.delete(lockKey);
    }
}
