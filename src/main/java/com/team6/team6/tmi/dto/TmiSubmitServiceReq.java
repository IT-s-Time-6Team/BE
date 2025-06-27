package com.team6.team6.tmi.dto;

import com.team6.team6.tmi.entity.TmiSubmission;

public record TmiSubmitServiceReq(String tmiContent, String roomKey, Long roomId, Long memberId, String memberName) {

    public static TmiSubmitServiceReq of(String tmiContent, String roomKey, Long roomId, Long memberId, String memberName) {
        return new TmiSubmitServiceReq(tmiContent, roomKey, roomId, memberId, memberName);
    }

    public TmiSubmission toEntity() {
        return TmiSubmission.create(roomId, memberId, memberName, tmiContent);
    }
}
