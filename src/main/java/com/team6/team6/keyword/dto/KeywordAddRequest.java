package com.team6.team6.keyword.dto;

import com.team6.team6.member.security.UserPrincipal;

public record KeywordAddRequest(String keyword) {
    public static KeywordAddRequest of(String keyword) {
        return new KeywordAddRequest(keyword);
    }

    public KeywordAddServiceReq toServiceRequest(String roomKey, UserPrincipal principal) {
        return KeywordAddServiceReq.of(
                this.keyword,
                roomKey,
                principal.getRoomId(),
                principal.getId()
        );
    }
}