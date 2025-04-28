package com.team6.team6.room.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String roomKey;
    private Integer requiredAgreements;
    private Integer maxMember;
    private LocalDateTime timeLimit;
    private String gameMode;
    private LocalDateTime closedAt;

    @Builder
    private Room(String roomKey, Integer requiredAgreements, Integer maxMember,
                 LocalDateTime timeLimit, String gameMode) {
        this.roomKey = roomKey;
        this.requiredAgreements = requiredAgreements;
        this.maxMember = maxMember;
        this.timeLimit = timeLimit;
        this.gameMode = gameMode;
    }

    public void closeRoom() {
        this.closedAt = LocalDateTime.now();
    }
}