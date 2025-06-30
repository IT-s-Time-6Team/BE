package com.team6.team6.tmi.domain;

import com.team6.team6.tmi.dto.MostIncorrectTmi;
import com.team6.team6.tmi.entity.TmiSubmission;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
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

    public List<MostIncorrectTmi> findMostIncorrectTmis(TmiVotes votes) {
        Map<String, Integer> incorrectCountByTmi = new HashMap<>();

        for (TmiSubmission submission : submissions) {
            String tmiContent = submission.getTmiContent();
            int incorrectVotes = votes.countIncorrectVotesForSubmission(submission.getDisplayOrder());
            incorrectCountByTmi.put(tmiContent, incorrectVotes);
        }

        if (incorrectCountByTmi.isEmpty()) {
            return Collections.emptyList();
        }

        int maxIncorrectCount = Collections.max(incorrectCountByTmi.values());

        return incorrectCountByTmi.entrySet().stream()
                .filter(entry -> entry.getValue() == maxIncorrectCount)
                .map(entry -> new MostIncorrectTmi(
                        entry.getKey(), entry.getValue()
                ))
                .collect(Collectors.toList());
    }
}
