package com.team6.team6.member.security;

import com.team6.team6.member.entity.Member;
import lombok.Getter;
import java.io.Serializable;

@Getter
public class UserPrincipal implements Serializable {
    private final Long id;
    private final String nickname;
    private final Long roomId;

    public UserPrincipal(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.roomId = member.getRoom().getId();
    }
}