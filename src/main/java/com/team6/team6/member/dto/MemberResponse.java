package com.team6.team6.member.dto;

import com.team6.team6.member.entity.Member;
import lombok.Builder;

public record MemberResponse(
    String nickname,
    Integer characterId,
    boolean isLeader
) {
    @Builder
    public MemberResponse {}
    
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getNickname(),
                member.getCharacterId(),
                member.isLeader()
        );
    }
}