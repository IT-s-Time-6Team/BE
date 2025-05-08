package com.team6.team6.keyword.domain;

import java.util.List;

public interface KeywordStore {

    void saveKeyword(Long roomId, String keyword);

    List<String> getKeywords(Long roomId);
}
