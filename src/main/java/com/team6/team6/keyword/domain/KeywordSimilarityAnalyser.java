package com.team6.team6.keyword.domain;

import java.util.List;

public interface KeywordSimilarityAnalyser {

    List<List<String>> analyse(List<String> keywords);
}
