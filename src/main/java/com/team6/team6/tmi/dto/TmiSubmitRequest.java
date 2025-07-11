package com.team6.team6.tmi.dto;

import com.team6.team6.member.security.UserPrincipal;
import jakarta.validation.constraints.NotBlank;

public record TmiSubmitRequest(
        @NotBlank(message = "TMI 내용을 입력해주세요")
        String tmiContent
) {
    public TmiSubmitServiceReq toServiceRequest(UserPrincipal userPrincipal) {
        return TmiSubmitServiceReq.of(tmiContent, userPrincipal.getRoomKey(),
                userPrincipal.getRoomId(), userPrincipal.getId(), userPrincipal.getNickname(), userPrincipal.getCharacter());
    }
}
