package com.team6.team6.room.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.room.dto.RoomCreateServiceRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String roomKey;
    private Integer requiredAgreements;
    private Integer maxMember;
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    private LocalDateTime closedAt;

    @Builder
    private Room(String roomKey, Integer requiredAgreements, Integer maxMember,
                 Integer durationMinutes, GameMode gameMode) {
        this.roomKey = roomKey;
        this.requiredAgreements = requiredAgreements;
        this.maxMember = maxMember;
        this.durationMinutes = durationMinutes;
        this.gameMode = gameMode;
    }

    public void closeRoom() {
        this.closedAt = LocalDateTime.now();
    }

    public static Room create(String roomKey, RoomCreateServiceRequest request) {
        return Room.builder()
                .roomKey(roomKey)
                .requiredAgreements(request.requiredAgreements())
                .maxMember(request.maxMember())
                .durationMinutes(request.durationMinutes())
                .gameMode(request.gameMode())
                .build();
    }
}