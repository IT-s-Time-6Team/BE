package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.entity.TmiSubmission;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.IntStream;

@Slf4j
public class TmiSubmissions {
    private final List<TmiSubmission> submissions;
    private final Random random = new Random();

    private TmiSubmissions(List<TmiSubmission> submissions) {
        this.submissions = List.copyOf(submissions);
    }

    public static TmiSubmissions from(List<TmiSubmission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            throw new IllegalArgumentException("TMI 제출 목록이 비어있습니다");
        }
        return new TmiSubmissions(submissions);
    }

    public TmiSubmissions shuffleForVoting() {
        List<TmiSubmission> shuffled = new ArrayList<>(submissions);
        Collections.shuffle(shuffled, random);

        // displayOrder를 순차적으로 설정
        IntStream.range(0, shuffled.size())
                .forEach(i -> shuffled.get(i).setDisplayOrder(i));

        log.debug("TMI 투표 목록 랜덤 배치 완료: size={}", shuffled.size());
        return new TmiSubmissions(shuffled);
    }

    public int getTotalCount() {
        return submissions.size();
    }

    public boolean isEmpty() {
        return submissions.isEmpty();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= submissions.size()) {
            throw new IllegalArgumentException("잘못된 TMI 인덱스: " + index + " (전체: " + submissions.size() + ")");
        }
    }
}
