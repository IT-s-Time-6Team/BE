package com.team6.team6.tmi.entity;

import com.team6.team6.global.entity.BaseEntity;
import com.team6.team6.member.entity.CharacterType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TmiVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private String voterName;              // 투표자
    private Long voterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voter_character_type")
    private CharacterType voterCharacterType;
    private String votedMemberName;        // 투표 받은 멤버
    private Long votedMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "voted_character_type")
    private CharacterType votedCharacterType;
    private Boolean isCorrect;          // 투표가 맞았는지 여부
    private Long tmiSubmissionId;          // 투표한 TMI
    private Integer votingRound;               // 몇 번째 TMI에 대한 투표인지

    @Builder
    private TmiVote(Long roomId, String voterName, Long voterId, CharacterType voterCharacterType,
                    String votedMemberName, Long votedMemberId, CharacterType votedCharacterType,
                    Long tmiSubmissionId, Integer votingRound, Boolean isCorrect) {
        this.roomId = roomId;
        this.voterName = voterName;
        this.voterId = voterId;
        this.voterCharacterType = voterCharacterType;
        this.votedMemberName = votedMemberName;
        this.votedMemberId = votedMemberId;
        this.votedCharacterType = votedCharacterType;
        this.tmiSubmissionId = tmiSubmissionId;
        this.votingRound = votingRound;
        this.isCorrect = isCorrect;
    }

    public static TmiVote create(Long roomId, String voterName, Long voterId, CharacterType voterCharacterType,
                                 String votedMemberName, Long votedMemberId, CharacterType votedCharacterType,
                                 Long tmiSubmissionId, int votingRound) {
        return TmiVote.builder()
                .roomId(roomId)
                .voterName(voterName)
                .voterId(voterId)
                .voterCharacterType(voterCharacterType)
                .votedMemberName(votedMemberName)
                .votedMemberId(votedMemberId)
                .votedCharacterType(votedCharacterType)
                .tmiSubmissionId(tmiSubmissionId)
                .votingRound(votingRound)
                .isCorrect(null)
                .build();
    }

    public void changeIsCorrect(String correctAnswer) {
        if (votedMemberName.equals(correctAnswer) || voterName.equals(correctAnswer)) {
            this.isCorrect = true;
        } else {
            this.isCorrect = false;
        }
    }
}
