package com.team6.team6.tmi.dto;

import com.team6.team6.member.entity.CharacterType;
import lombok.Builder;

@Builder
public record TmiVoteServiceReq(
        String roomKey,
        Long roomId,
        String voterName,
        Long voterId,
        CharacterType voterCharacterType,
        String votedMemberName
) {
    public static TmiVoteServiceReq of(String roomKey, Long roomId, String voterName, Long voterId,
                                       CharacterType voterCharacterType, String votedMemberName) {
        return TmiVoteServiceReq.builder()
                .roomKey(roomKey)
                .roomId(roomId)
                .voterName(voterName)
                .voterId(voterId)
                .voterCharacterType(voterCharacterType)
                .votedMemberName(votedMemberName)
                .build();
    }
}
