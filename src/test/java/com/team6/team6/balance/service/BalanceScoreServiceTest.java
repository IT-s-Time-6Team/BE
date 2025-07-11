package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.repository.BalanceMemberScoreRepository;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceMemberScoreInfo;
import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceMemberScore;
import com.team6.team6.balance.entity.BalanceVote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceScoreService 테스트")
class BalanceScoreServiceTest {

    @Mock
    private BalanceMemberScoreRepository balanceMemberScoreRepository;

    @Mock
    private BalanceVoteRepository balanceVoteRepository;

    @InjectMocks
    private BalanceScoreService balanceScoreService;

    @Test
    @DisplayName("멤버 점수 초기화 - 정상 케이스")
    void initializeMemberScores_Success_Test() {
        // given
        Long roomId = 1L;
        List<String> memberNames = List.of("user1", "user2", "user3");
        List<Long> memberIds = List.of(1L, 2L, 3L);

        when(balanceMemberScoreRepository.existsByRoomIdAndMemberId(roomId, 1L)).thenReturn(false);

        // when
        balanceScoreService.initializeMemberScores(roomId, memberNames, memberIds);

        // then
        verify(balanceMemberScoreRepository).saveAll(anyList());
        verify(balanceMemberScoreRepository).existsByRoomIdAndMemberId(roomId, 1L);
    }

    @Test
    @DisplayName("멤버 점수 초기화 - 이미 초기화된 경우 스킵")
    void initializeMemberScores_AlreadyExists_Test() {
        // given
        Long roomId = 1L;
        List<String> memberNames = List.of("user1", "user2");
        List<Long> memberIds = List.of(1L, 2L);

        when(balanceMemberScoreRepository.existsByRoomIdAndMemberId(roomId, 1L)).thenReturn(true);

        // when
        balanceScoreService.initializeMemberScores(roomId, memberNames, memberIds);

        // then
        verify(balanceMemberScoreRepository).existsByRoomIdAndMemberId(roomId, 1L);
        verify(balanceMemberScoreRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("점수 계산 및 업데이트 - A가 다수파")
    void calculateAndUpdateScores_ChoiceAMajority_Test() {
        // given
        Long roomId = 1L;
        int round = 0;

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.A, round, 1L);
        BalanceVote vote3 = BalanceVote.create(roomId, "user3", 3L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2, vote3);

        BalanceMemberScore score1 = BalanceMemberScore.createInitial(roomId, 1L, "user1");
        BalanceMemberScore score2 = BalanceMemberScore.createInitial(roomId, 2L, "user2");
        BalanceMemberScore score3 = BalanceMemberScore.createInitial(roomId, 3L, "user3");

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);
        when(balanceMemberScoreRepository.findByRoomIdAndMemberId(roomId, 1L)).thenReturn(Optional.of(score1));
        when(balanceMemberScoreRepository.findByRoomIdAndMemberId(roomId, 2L)).thenReturn(Optional.of(score2));
        when(balanceMemberScoreRepository.findByRoomIdAndMemberId(roomId, 3L)).thenReturn(Optional.of(score3));

        // when
        balanceScoreService.calculateAndUpdateScores(roomId, round);

        // then
        assertSoftly(softly -> {
            softly.assertThat(score1.getCurrentScore()).isEqualTo(1); // 다수파 +1
            softly.assertThat(score2.getCurrentScore()).isEqualTo(1); // 다수파 +1
            softly.assertThat(score3.getCurrentScore()).isEqualTo(-1); // 소수파 -1
        });
        verify(balanceMemberScoreRepository, times(3)).save(any(BalanceMemberScore.class));
    }

    @Test
    @DisplayName("점수 계산 및 업데이트 - 동점인 경우")
    void calculateAndUpdateScores_Tie_Test() {
        // given
        Long roomId = 1L;
        int round = 0;

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        balanceScoreService.calculateAndUpdateScores(roomId, round);

        // then - 동점이므로 점수 업데이트 없음
        verify(balanceMemberScoreRepository, never()).save(any(BalanceMemberScore.class));
    }

    @Test
    @DisplayName("모든 멤버 점수 조회 - 동점자 순위 처리")
    void getAllMemberScores_WithTieRanking_Test() {
        // given
        Long roomId = 1L;

        BalanceMemberScore score1 = BalanceMemberScore.createInitial(roomId, 1L, "user1");
        score1.addScore(3); // 3점
        BalanceMemberScore score2 = BalanceMemberScore.createInitial(roomId, 2L, "user2");
        score2.addScore(3); // 3점 (동점)
        BalanceMemberScore score3 = BalanceMemberScore.createInitial(roomId, 3L, "user3");
        score3.addScore(1); // 1점
        BalanceMemberScore score4 = BalanceMemberScore.createInitial(roomId, 4L, "user4");
        score4.addScore(0); // 0점

        List<BalanceMemberScore> scores = List.of(score1, score2, score3, score4); // 점수 순 정렬됨

        when(balanceMemberScoreRepository.findByRoomIdOrderByCurrentScoreDesc(roomId)).thenReturn(scores);

        // when
        List<BalanceMemberScoreInfo> result = balanceScoreService.getAllMemberScores(roomId);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(4);
            softly.assertThat(result.get(0).rank()).isEqualTo(1); // user1: 1등
            softly.assertThat(result.get(1).rank()).isEqualTo(1); // user2: 1등 (동점)
            softly.assertThat(result.get(2).rank()).isEqualTo(3); // user3: 3등 (2등 건너뜀)
            softly.assertThat(result.get(3).rank()).isEqualTo(4); // user4: 4등
        });
    }

    @Test
    @DisplayName("특정 멤버 점수 조회")
    void getMemberScore_Test() {
        // given
        Long roomId = 1L;
        String memberName = "user2";

        BalanceMemberScore targetScore = BalanceMemberScore.createInitial(roomId, 2L, "user2");
        targetScore.addScore(2);

        BalanceMemberScore score1 = BalanceMemberScore.createInitial(roomId, 1L, "user1");
        score1.addScore(3);
        BalanceMemberScore score3 = BalanceMemberScore.createInitial(roomId, 3L, "user3");
        score3.addScore(1);

        List<BalanceMemberScore> allScores = List.of(score1, targetScore, score3); // 점수 순

        when(balanceMemberScoreRepository.findByRoomIdAndMemberName(roomId, memberName))
                .thenReturn(Optional.of(targetScore));
        when(balanceMemberScoreRepository.findByRoomIdOrderByCurrentScoreDesc(roomId))
                .thenReturn(allScores);

        // when
        BalanceMemberScoreInfo result = balanceScoreService.getMemberScore(roomId, memberName);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.memberName()).isEqualTo("user2");
            softly.assertThat(result.currentScore()).isEqualTo(2);
            softly.assertThat(result.rank()).isEqualTo(2); // 2등
        });
    }

    @Test
    @DisplayName("특정 멤버 점수 조회 - 존재하지 않는 멤버")
    void getMemberScore_NotFound_Test() {
        // given
        Long roomId = 1L;
        String memberName = "nonexistent";

        when(balanceMemberScoreRepository.findByRoomIdAndMemberName(roomId, memberName))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> balanceScoreService.getMemberScore(roomId, memberName))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("멤버 점수를 찾을 수 없습니다: " + memberName);
    }

    @Test
    @DisplayName("개별 멤버 점수 변화량 조회 - 다수파")
    void getScoreChange_Majority_Test() {
        // given
        Long roomId = 1L;
        String memberName = "user1";
        int round = 0;
        BalanceChoice majorityChoice = BalanceChoice.A;

        BalanceVote vote = BalanceVote.create(roomId, memberName, 1L, BalanceChoice.A, round, 1L);
        List<BalanceVote> votes = List.of(vote);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        int scoreChange = balanceScoreService.getScoreChange(roomId, memberName, round, majorityChoice);

        // then
        assertThat(scoreChange).isEqualTo(1); // 다수파 +1
    }

    @Test
    @DisplayName("개별 멤버 점수 변화량 조회 - 소수파")
    void getScoreChange_Minority_Test() {
        // given
        Long roomId = 1L;
        String memberName = "user1";
        int round = 0;
        BalanceChoice majorityChoice = BalanceChoice.A;

        BalanceVote vote = BalanceVote.create(roomId, memberName, 1L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        int scoreChange = balanceScoreService.getScoreChange(roomId, memberName, round, majorityChoice);

        // then
        assertThat(scoreChange).isEqualTo(-1); // 소수파 -1
    }

    @Test
    @DisplayName("개별 멤버 점수 변화량 조회 - 동점")
    void getScoreChange_Tie_Test() {
        // given
        Long roomId = 1L;
        String memberName = "user1";
        int round = 0;
        BalanceChoice majorityChoice = null; // 동점

        BalanceVote vote = BalanceVote.create(roomId, memberName, 1L, BalanceChoice.A, round, 1L);
        List<BalanceVote> votes = List.of(vote);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        int scoreChange = balanceScoreService.getScoreChange(roomId, memberName, round, majorityChoice);

        // then
        assertThat(scoreChange).isZero(); // 동점 0점
    }

    @Test
    @DisplayName("라운드별 모든 멤버 점수 변화량 조회")
    void getScoreChangesForRound_Test() {
        // given
        Long roomId = 1L;
        int round = 0;
        BalanceChoice majorityChoice = BalanceChoice.A;

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.A, round, 1L);
        BalanceVote vote3 = BalanceVote.create(roomId, "user3", 3L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2, vote3);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        Map<String, Integer> scoreChanges = balanceScoreService.getScoreChangesForRound(roomId, round, majorityChoice);

        // then
        assertSoftly(softly -> {
            softly.assertThat(scoreChanges).hasSize(3);
            softly.assertThat(scoreChanges.get("user1")).isEqualTo(1); // 다수파
            softly.assertThat(scoreChanges.get("user2")).isEqualTo(1); // 다수파
            softly.assertThat(scoreChanges.get("user3")).isEqualTo(-1); // 소수파
        });
    }

    @Test
    @DisplayName("라운드별 모든 멤버 점수 변화량 조회 - 동점")
    void getScoreChangesForRound_Tie_Test() {
        // given
        Long roomId = 1L;
        int round = 0;
        BalanceChoice majorityChoice = null; // 동점

        BalanceVote vote1 = BalanceVote.create(roomId, "user1", 1L, BalanceChoice.A, round, 1L);
        BalanceVote vote2 = BalanceVote.create(roomId, "user2", 2L, BalanceChoice.B, round, 1L);
        List<BalanceVote> votes = List.of(vote1, vote2);

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(votes);

        // when
        Map<String, Integer> scoreChanges = balanceScoreService.getScoreChangesForRound(roomId, round, majorityChoice);

        // then
        assertSoftly(softly -> {
            softly.assertThat(scoreChanges).hasSize(2);
            softly.assertThat(scoreChanges.get("user1")).isZero(); // 동점
            softly.assertThat(scoreChanges.get("user2")).isZero(); // 동점
        });
    }

    @Test
    @DisplayName("투표를 찾을 수 없는 경우 예외")
    void getScoreChange_VoteNotFound_Test() {
        // given
        Long roomId = 1L;
        String memberName = "nonexistent";
        int round = 0;
        BalanceChoice majorityChoice = BalanceChoice.A;

        when(balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> balanceScoreService.getScoreChange(roomId, memberName, round, majorityChoice))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("투표를 찾을 수 없습니다");
    }
} 