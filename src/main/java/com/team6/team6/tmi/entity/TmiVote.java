package com.team6.team6.tmi.entity;

import com.team6.team6.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private String votedMemberName;        // 투표 받은 멤버
    private Boolean isCorrect;          // 투표가 맞았는지 여부
    private Long tmiSubmissionId;          // 투표한 TMI
    private Integer votingRound;               // 몇 번째 TMI에 대한 투표인지

    @Builder
    private TmiVote(Long roomId, String voterName, String votedMemberName,
                    Boolean isCorrect, Long tmiSubmissionId, Integer votingRound) {
        this.roomId = roomId;
        this.voterName = voterName;
        this.votedMemberName = votedMemberName;
        this.isCorrect = isCorrect;
        this.tmiSubmissionId = tmiSubmissionId;
        this.votingRound = votingRound;
    }

    public static TmiVote create(Long roomId, String voterName, String votedMemberName,
                                 Long tmiSubmissionId, int votingRound) {
        return TmiVote.builder()
                .roomId(roomId)
                .voterName(voterName)
                .votedMemberName(votedMemberName)
                .isCorrect(null)
                .tmiSubmissionId(tmiSubmissionId)
                .votingRound(votingRound)
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
