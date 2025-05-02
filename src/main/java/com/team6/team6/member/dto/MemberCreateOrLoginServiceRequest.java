package com.team6.team6.member.dto;

import lombok.Builder;

public record MemberCreateOrLoginServiceRequest(
        String nickname,
        String password
) {
    @Builder
    public MemberCreateOrLoginServiceRequest {}
}