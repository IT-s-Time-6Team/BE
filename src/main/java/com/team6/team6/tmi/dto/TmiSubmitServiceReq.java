package com.team6.team6.tmi.dto;

import com.team6.team6.tmi.entity.TmiSubmission;

public record TmiSubmitServiceReq(String tmiContent, String roomKey, Long roomId, Long memberId) {

    public static TmiSubmitServiceReq of(String tmiContent, String roomKey, Long roomId, Long memberId) {
        return new TmiSubmitServiceReq(tmiContent, roomKey, roomId, memberId);
    }

    public TmiSubmission toEntity() {
        return TmiSubmission.create(roomId, memberId, tmiContent);
    }
}
