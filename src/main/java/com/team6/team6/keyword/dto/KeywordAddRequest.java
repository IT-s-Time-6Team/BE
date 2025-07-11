package com.team6.team6.keyword.dto;

import com.team6.team6.member.security.UserPrincipal;
import jakarta.validation.constraints.NotNull;

public record KeywordAddRequest(
        @NotNull(message = "키워드를 입력해주세요")
        String keyword
) {
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