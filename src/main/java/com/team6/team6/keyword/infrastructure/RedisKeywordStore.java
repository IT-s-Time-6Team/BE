package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.KeywordStore;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Profile("!test")
public class RedisKeywordStore implements KeywordStore {

    private static final String KEY_PREFIX = "keywords:";
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisKeywordStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void saveKeyword(Long roomId, String keyword) {
        String key = generateKey(roomId);
        List<String> keywords = getKeywordsFromRedis(roomId);
        keywords.add(keyword);
        redisTemplate.opsForValue().set(key, keywords);
    }

    @Override
    public List<String> getKeywords(Long roomId) {
        return getKeywordsFromRedis(roomId);
    }

    @Override
    public void deleteKeywordsByRoomId(Long roomId) {
        String key = generateKey(roomId);
        redisTemplate.delete(key);
    }

    private String generateKey(Long roomId) {
        return KEY_PREFIX + roomId;
    }

    private List<String> getKeywordsFromRedis(Long roomId) {
        String key = generateKey(roomId);
        List<String> keywords = (List<String>) redisTemplate.opsForValue().get(key);
        return keywords != null ? keywords : new ArrayList<>();
    }
}
