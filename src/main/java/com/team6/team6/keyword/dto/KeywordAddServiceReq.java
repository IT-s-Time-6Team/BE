package com.team6.team6.keyword.dto;

import com.team6.team6.keyword.entity.Keyword;

public record KeywordAddServiceReq(String keyword, String roomKey, Long roomId, Long memberId) {

    public static KeywordAddServiceReq of(String keyword, String roomKey, Long roomId, Long memberId) {
        return new KeywordAddServiceReq(keyword, roomKey, roomId, memberId);
    }

    public Keyword toEntity() {
        return Keyword.builder()
                .keyword(this.keyword)
                .roomId(this.roomId)
                .memberId(this.memberId)
                .build();
    }
}