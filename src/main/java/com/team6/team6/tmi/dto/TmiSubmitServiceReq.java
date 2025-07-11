package com.team6.team6.tmi.dto;

import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.tmi.entity.TmiSubmission;

public record TmiSubmitServiceReq(String tmiContent, String roomKey, Long roomId, Long memberId, String memberName,
                                  CharacterType characterType) {

    public static TmiSubmitServiceReq of(String tmiContent, String roomKey, Long roomId, Long memberId, String memberName, CharacterType characterType) {
        return new TmiSubmitServiceReq(tmiContent, roomKey, roomId, memberId, memberName, characterType);
    }

    public TmiSubmission toEntity() {
        return TmiSubmission.create(roomId, memberId, memberName, tmiContent, characterType);
    }
}
