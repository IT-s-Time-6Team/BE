package com.team6.team6.room.dto;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public record RoomResult(
        List<String> sharedKeywords,
        String totalDuration,
        List<String> topKeywordContributorNames,
        int topKeywordCount,
        List<String> mostMatchedHobbyUserNames,
        int matchedHobbyCount
) {

    public static RoomResult of(
            List<String> sharedKeywords,
            Duration totalDuration,
            List<MemberKeywordCount> topKeywordContributors,
            List<MemberKeywordCount> mostSharedKeywordUsers
    ) {
        String formattedDuration = formatDuration(totalDuration);

        // 가장 많은 키워드를 생성한 멤버 정보
        int topKeywordCount = topKeywordContributors.isEmpty() ? 0 : topKeywordContributors.get(0).keywordCount();
        List<String> topKeywordContributorNames = getMemberNames(topKeywordContributors);

        // 공감 키워드가 가장 많은 멤버 정보
        int matchedHobbyCount = mostSharedKeywordUsers.isEmpty() ? 0 : mostSharedKeywordUsers.get(0).keywordCount();
        List<String> mostMatchedHobbyUserNames = getMemberNames(mostSharedKeywordUsers);

        return new RoomResult(
                sharedKeywords,
                formattedDuration,
                topKeywordContributorNames,
                topKeywordCount,
                mostMatchedHobbyUserNames,
                matchedHobbyCount
        );
    }

    private static List<String> getMemberNames(List<MemberKeywordCount> memberKeywordCounts) {
        return memberKeywordCounts.stream()
                .map(MemberKeywordCount::memberName)
                .collect(Collectors.toList());
    }

    private static String formatDuration(Duration totalDuration) {
        long totalSeconds = totalDuration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, seconds);
        } else {
            return String.format("%d초", seconds);
        }
    }
}
