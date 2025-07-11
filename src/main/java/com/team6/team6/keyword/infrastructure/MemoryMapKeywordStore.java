package com.team6.team6.keyword.infrastructure;

import com.team6.team6.keyword.domain.KeywordStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("test")
public class MemoryMapKeywordStore implements KeywordStore {

    private final Map<Long, List<String>> keywordStore = new ConcurrentHashMap<>();

    @Override
    public void saveKeyword(Long roomId, String keyword) {
        if (!keywordStore.containsKey(roomId)) {
            keywordStore.put(roomId, new ArrayList<>());
        }
        keywordStore.get(roomId).add(keyword);
    }

    @Override
    public List<String> getKeywords(Long roomId) {
        return keywordStore.getOrDefault(roomId, Collections.emptyList());
    }

    @Override
    public void deleteKeywordsByRoomId(Long roomId) {
        keywordStore.remove(roomId);
    }
}
