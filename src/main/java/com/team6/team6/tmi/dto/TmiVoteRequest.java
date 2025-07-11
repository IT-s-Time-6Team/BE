package com.team6.team6.tmi.dto;

import com.team6.team6.member.security.UserPrincipal;
import jakarta.validation.constraints.NotBlank;

public record TmiVoteRequest(
        @NotBlank(message = "투표할 멤버를 선택해주세요")
        String votedMemberName
) {
    public TmiVoteServiceReq toServiceRequest(String roomKey, UserPrincipal userPrincipal) {
        return TmiVoteServiceReq.of(
                roomKey,
                userPrincipal.getRoomId(),
                userPrincipal.getNickname(),
                userPrincipal.getId(),
                userPrincipal.getCharacter(),
                this.votedMemberName
        );
    }
}
