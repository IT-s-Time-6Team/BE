package com.team6.team6.member.security;

import com.team6.team6.member.entity.CharacterType;
import com.team6.team6.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    private final Long id;
    private final String nickname;
    private final Long roomId;
    private final String roomKey;
    private final CharacterType character;
    private final String gameMode;

    public UserPrincipal(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.roomId = member.getRoom().getId();
        this.roomKey = member.getRoom().getRoomKey();
        this.character = member.getCharacter();
        this.gameMode = member.getRoom().getGameMode().name();
    }
}
