package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceVoteResult;
import com.team6.team6.balance.domain.BalanceVotes;
import com.team6.team6.balance.domain.repository.BalanceMemberScoreRepository;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceMemberScoreInfo;
import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceMemberScore;
import com.team6.team6.balance.entity.BalanceVote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BalanceScoreService {

    private final BalanceMemberScoreRepository balanceMemberScoreRepository;
    private final BalanceVoteRepository balanceVoteRepository;

    @Transactional
    public void initializeMemberScores(Long roomId, List<String> memberNames, List<Long> memberIds) {
        // 이미 초기화된 경우 스킵
        if (balanceMemberScoreRepository.existsByRoomIdAndMemberId(roomId, memberIds.get(0))) {
            return;
        }

        List<BalanceMemberScore> initialScores = IntStream.range(0, memberNames.size())
                .mapToObj(i -> BalanceMemberScore.createInitial(roomId, memberIds.get(i), memberNames.get(i)))
                .toList();

        balanceMemberScoreRepository.saveAll(initialScores);
        log.info("밸런스 모드 멤버 점수 초기화: roomId={}, memberCount={}", roomId, memberNames.size());
    }

    @Transactional
    public void calculateAndUpdateScores(Long roomId, int round) {
        // 해당 라운드의 모든 투표 조회
        List<BalanceVote> votes = balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round);
        BalanceVotes balanceVotes = BalanceVotes.from(votes);
        BalanceVoteResult voteResult = balanceVotes.calculateVoteResult();

        log.debug("라운드 {} 점수 계산 시작: A={}, B={}, isTie={}", 
                round, voteResult.choiceACount(), voteResult.choiceBCount(), voteResult.isTie());

        // 동률이면 점수 변화 없음
        if (voteResult.isTie()) {
            log.debug("동률로 인해 점수 변화 없음: round={}", round);
            return;
        }

        // 각 멤버의 점수 업데이트
        for (BalanceVote vote : votes) {
            updateMemberScore(vote, voteResult.majorityChoice());
        }
    }

    private void updateMemberScore(BalanceVote vote, BalanceChoice majorityChoice) {
        BalanceMemberScore memberScore = balanceMemberScoreRepository
                .findByRoomIdAndMemberId(vote.getRoomId(), vote.getMemberId())
                .orElseThrow(() -> new IllegalStateException("멤버 점수를 찾을 수 없습니다: " + vote.getMemberId()));

        boolean isMajority = vote.getSelectedChoice() == majorityChoice;
        memberScore.updateScore(isMajority);

        balanceMemberScoreRepository.save(memberScore);
        
        log.debug("멤버 점수 업데이트: member={}, choice={}, isMajority={}, newScore={}", 
                vote.getVoterName(), vote.getSelectedChoice(), isMajority, memberScore.getCurrentScore());
    }

    public List<BalanceMemberScoreInfo> getAllMemberScores(Long roomId) {
        List<BalanceMemberScore> scores = balanceMemberScoreRepository.findByRoomIdOrderByCurrentScoreDesc(roomId);
        
        // 동점자 처리를 위한 순위 계산
        List<BalanceMemberScoreInfo> result = new ArrayList<>();
        int currentRank = 1;
        
        for (int i = 0; i < scores.size(); i++) {
            BalanceMemberScore score = scores.get(i);
            
            // 이전 점수와 다르면 현재 인덱스 + 1이 새로운 순위
            if (i > 0 && !scores.get(i - 1).getCurrentScore().equals(score.getCurrentScore())) {
                currentRank = i + 1;
            }
            
            result.add(BalanceMemberScoreInfo.from(score, currentRank));
        }
        
        return result;
    }

    public BalanceMemberScoreInfo getMemberScore(Long roomId, String memberName) {
        BalanceMemberScore score = balanceMemberScoreRepository
                .findByRoomIdAndMemberName(roomId, memberName)
                .orElseThrow(() -> new IllegalStateException("멤버 점수를 찾을 수 없습니다: " + memberName));

        // 순위 계산
        List<BalanceMemberScore> allScores = balanceMemberScoreRepository.findByRoomIdOrderByCurrentScoreDesc(roomId);
        int rank = IntStream.range(0, allScores.size())
                .filter(i -> allScores.get(i).getMemberName().equals(memberName))
                .findFirst()
                .orElse(-1) + 1; // 0-based to 1-based

        return BalanceMemberScoreInfo.from(score, rank);
    }

    public int getScoreChange(Long roomId, String memberName, int round, BalanceChoice majorityChoice) {
        BalanceVote vote = balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round)
                .stream()
                .filter(v -> v.getVoterName().equals(memberName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("투표를 찾을 수 없습니다"));

        // 동률인 경우 점수 변화 없음
        if (majorityChoice == null) {
            return 0;
        }

        // 다수파: +1, 소수파: -1
        return vote.getSelectedChoice() == majorityChoice ? 1 : -1;
    }

    public Map<String, Integer> getScoreChangesForRound(Long roomId, int round, BalanceChoice majorityChoice) {
        List<BalanceVote> votes = balanceVoteRepository.findByRoomIdAndVotingRound(roomId, round);
        
        return votes.stream()
                .collect(Collectors.toMap(
                        BalanceVote::getVoterName,
                        vote -> {
                            if (majorityChoice == null) return 0; // 동률
                            return vote.getSelectedChoice() == majorityChoice ? 1 : -1;
                        }
                ));
    }
} 