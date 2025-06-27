package com.team6.team6.tmi.dto;

import lombok.Builder;

@Builder
public record TmiVoteServiceReq(
        String roomKey,
        Long roomId,
        String voterName,
        String votedMemberName
) {
    public static TmiVoteServiceReq of(String roomKey, Long roomId, String voterName, String votedMemberName) {
        return TmiVoteServiceReq.builder()
                .roomKey(roomKey)
                .roomId(roomId)
                .voterName(voterName)
                .votedMemberName(votedMemberName)
                .build();
    }
}
