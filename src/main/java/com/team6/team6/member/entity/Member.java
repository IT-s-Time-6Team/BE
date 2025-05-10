package com.team6.team6.member.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.room.entity.Room;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String password;

    private boolean isLeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type")
    private CharacterType character;

    @Builder
    private Member(String nickname, String password, boolean isLeader, Room room, CharacterType character) {
        this.nickname = nickname;
        this.password = password;
        this.isLeader = isLeader;
        this.room = room;
        this.character = character;
    }

    public static Member create(String nickname, String password, Room room, Integer characterOrder, boolean isLeader) {
        CharacterType characterType = CharacterType.fromOrder(characterOrder);

        return Member.builder()
                .nickname(nickname)
                .password(password)
                .room(room)
                .character(characterType)
                .isLeader(isLeader)
                .build();
    }
}