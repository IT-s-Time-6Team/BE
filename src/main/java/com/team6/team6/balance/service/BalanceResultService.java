package com.team6.team6.balance.service;

import com.team6.team6.balance.domain.BalanceMessagePublisher;
import com.team6.team6.balance.domain.BalanceVoteResult;
import com.team6.team6.balance.domain.repository.BalanceVoteRepository;
import com.team6.team6.balance.dto.BalanceFinalResultResponse;
import com.team6.team6.balance.dto.BalanceMemberScoreInfo;
import com.team6.team6.balance.dto.BalanceQuestionResponse;
import com.team6.team6.balance.dto.BalanceQuestionSummary;
import com.team6.team6.balance.dto.BalanceRoundResultResponse;
import com.team6.team6.balance.entity.BalanceChoice;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import com.team6.team6.balance.entity.BalanceSession;
import com.team6.team6.balance.entity.BalanceVote;
import com.team6.team6.balance.entity.BalanceSessionQuestion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BalanceResultService {

    private final BalanceSessionService balanceSessionService;
    private final BalanceQuestionService balanceQuestionService;
    private final BalanceVoteService balanceVoteService;
    private final BalanceScoreService balanceScoreService;
    private final BalanceMessagePublisher messagePublisher;
    private final BalanceRevealService balanceRevealService;
    private final BalanceVoteRepository balanceVoteRepository;



    public BalanceRoundResultResponse getLatestVotingResult(Long roomId, String memberName) {
        BalanceSession session = balanceSessionService.findSessionByRoomId(roomId);
        
        // 현재 라운드의 투표 결과 조회 (가장 최근 완료된 라운드)
        int currentRound = session.getCurrentQuestionIndex() + 1; // 1-based 라운드
        
        // 해당 라운드의 투표 결과 조회
        BalanceVoteResult voteResult = balanceVoteService.getRoundVoteResult(roomId, session.getCurrentQuestionIndex());

        // 해당 라운드의 문제 조회 (display_order는 0-based이므로 -1)
        BalanceSessionQuestion question = balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, currentRound - 1);
        
        // 사용자의 실제 선택 조회
        BalanceChoice userChoice = getUserChoice(roomId, memberName, session.getCurrentQuestionIndex());
        
        // 점수 변화 계산
        int scoreChange = calculateScoreChange(voteResult, userChoice);
        
        // 현재 점수 및 순위 조회
        List<BalanceMemberScoreInfo> allScores = balanceScoreService.getAllMemberScores(roomId);
        
        // 각 멤버의 이번 라운드 점수 변화량 계산
        Map<String, Integer> scoreChanges = balanceScoreService.getScoreChangesForRound(
                roomId, session.getCurrentQuestionIndex(), voteResult.majorityChoice());
        
        // allMemberScores의 scoreChange를 실제 계산된 값으로 업데이트
        List<BalanceMemberScoreInfo> updatedAllScores = allScores.stream()
                .map(score -> BalanceMemberScoreInfo.of(
                        score.memberName(),
                        score.currentScore(),
                        scoreChanges.getOrDefault(score.memberName(), 0), // 실제 계산된 scoreChange
                        score.rank()
                ))
                .toList();
        
        BalanceMemberScoreInfo memberScore = updatedAllScores.stream()
                .filter(score -> score.memberName().equals(memberName))
                .findFirst()
                .orElse(BalanceMemberScoreInfo.builder()
                        .memberName(memberName)
                        .currentScore(0)
                        .rank(1)
                        .build());

        // 퍼센트 계산
        long totalVotes = voteResult.choiceACount() + voteResult.choiceBCount();
        double choiceAPercentage = totalVotes > 0 ? (double) voteResult.choiceACount() / totalVotes * 100 : 0;
        double choiceBPercentage = totalVotes > 0 ? (double) voteResult.choiceBCount() / totalVotes * 100 : 0;

        return BalanceRoundResultResponse.builder()
                .myChoice(userChoice)
                .choiceACount((int) voteResult.choiceACount())
                .choiceBCount((int) voteResult.choiceBCount())
                .choiceAPercentage(choiceAPercentage)
                .choiceBPercentage(choiceBPercentage)
                .majorityChoice(voteResult.majorityChoice())
                .isTie(voteResult.isTie())
                .scoreChange(scoreChange)
                .currentScore(memberScore.currentScore())
                .currentRank(memberScore.rank())
                .currentRound(currentRound)
                .allMemberScores(updatedAllScores)
                .build();
    }

    public BalanceFinalResultResponse getSessionResults(Long roomId, String memberName) {
        BalanceSession session = balanceSessionService.findSessionByRoomId(roomId);

        // 게임 완료 상태 검증
        session.requireGameCompleted();

        // 최종 점수 순위 조회
        List<BalanceMemberScoreInfo> finalScores = balanceScoreService.getAllMemberScores(roomId);

        // 점수 데이터가 없는 경우 예외 처리
        if (finalScores.isEmpty()) {
            throw new IllegalStateException("최종 점수가 존재하지 않습니다: roomId=" + roomId);
        }

        // 해당 멤버의 최종 정보 조회
        BalanceMemberScoreInfo memberScore = finalScores.stream()
                .filter(score -> score.memberName().equals(memberName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("멤버를 찾을 수 없습니다: " + memberName));

        // 우승자 결정 (최고 점수를 가진 모든 멤버들)
        int highestScore = finalScores.get(0).currentScore();
        List<String> winnerNicknames = finalScores.stream()
                .filter(score -> score.currentScore() == highestScore)
                .map(BalanceMemberScoreInfo::memberName)
                .toList();

        // 각 라운드별 결과 요약 생성
        List<BalanceRoundResultResponse> roundResults = generateRoundResultsSummary(roomId, session.getTotalQuestions());

        // 가장 균형 잡힌 문제들 (50:50에 가까운 문제들)
        List<BalanceQuestionSummary> mostBalancedQuestions = findMostBalancedQuestions(roundResults, roomId);
        
        // 가장 만장일치에 가까운 문제들
        List<BalanceQuestionSummary> mostUnanimousQuestions = findMostUnanimousQuestions(roundResults, roomId);

        return BalanceFinalResultResponse.builder()
                .memberName(memberScore.memberName())
                .finalScore(memberScore.currentScore())
                .finalRank(memberScore.rank())
                .winnerNicknames(winnerNicknames)
                .mostBalancedQuestions(mostBalancedQuestions)
                .mostUnanimousQuestions(mostUnanimousQuestions)
                .build();
    }

    private BalanceRoundResultResponse getRoundResult(Long roomId, int round) {
        // 해당 라운드의 투표 결과 조회 (votingRound는 0-based이므로 -1)
        BalanceVoteResult voteResult = balanceVoteService.getRoundVoteResult(roomId, round - 1);

        // 해당 라운드의 문제 조회 (display_order는 0-based이므로 -1)
        BalanceSessionQuestion question = balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, round - 1);
        BalanceSession session = balanceSessionService.findSessionByRoomId(roomId);

        // 각 멤버의 점수 변화 조회
        Map<String, Integer> scoreChanges = balanceScoreService.getScoreChangesForRound(
                roomId, round, voteResult.majorityChoice());

        // 퍼센트 계산
        long totalVotes = voteResult.choiceACount() + voteResult.choiceBCount();
        double choiceAPercentage = totalVotes > 0 ? (double) voteResult.choiceACount() / totalVotes * 100 : 0;
        double choiceBPercentage = totalVotes > 0 ? (double) voteResult.choiceBCount() / totalVotes * 100 : 0;
        
        return BalanceRoundResultResponse.builder()
                .choiceACount((int) voteResult.choiceACount())
                .choiceBCount((int) voteResult.choiceBCount())
                .choiceAPercentage(choiceAPercentage)
                .choiceBPercentage(choiceBPercentage)
                .majorityChoice(voteResult.majorityChoice())
                .isTie(voteResult.isTie())
                .currentRound(round)
                .build();
    }

    @Transactional
    public void processGameReady(String roomKey, Long roomId, String memberName) {
        BalanceSession session = balanceSessionService.findSessionByRoomIdWithLock(roomId);

        // 게임이 이미 완료된 경우 아무 작업 없이 반환
        if (session.isCompletedPhase()) {
            log.debug("게임이 이미 완료된 상태입니다: roomKey={}, memberName={}", roomKey, memberName);
            return;
        }

        // 상태 검증
        session.requireResultViewPhase();

        // 결과 확인 완료 처리
        boolean allReady = session.processResultViewReady();

        log.debug("결과 확인 완료 처리: roomKey={}, memberName={}, progress={}/{}",
                roomKey, memberName, session.getCurrentResultViewedCount(), session.getTotalMembers());

        if (allReady) {
            // 모든 멤버가 결과 확인 완료
            log.debug("모든 멤버가 결과 확인 완료: roomKey={}", roomKey);

            if (session.isLastQuestionIndex()) {
                // 마지막 문제 완료 - 게임 종료
                session.completeGame();

                log.debug("밸런스 게임 완료: roomKey={}", roomKey);

                messagePublisher.notifyBalanceGameCompleted(roomKey);
            } else {
                // 다음 문제로 이동
                session.moveToNextQuestion();
                session.startQuestionRevealPhase();

                log.debug("자동으로 다음 문제 공개 시작: roomKey={}, questionIndex={}", 
                        roomKey, session.getCurrentQuestionIndex());

                // 다음 문제 공개 시작
                balanceRevealService.startQuestionReveal(roomKey, roomId);
            }
        } else {
            // 아직 대기 중인 멤버가 있음
            int currentCount = session.getCurrentResultViewedCount();
            int totalCount = session.getTotalMembers();
            messagePublisher.notifyBalanceGameReady(roomKey, currentCount, totalCount);
        }
    }



    private List<BalanceRoundResultResponse> generateRoundResultsSummary(Long roomId, int totalRounds) {
        return IntStream.range(1, totalRounds + 1)
                .mapToObj(round -> {
                    try {
                        return getRoundResult(roomId, round);
                    } catch (Exception e) {
                        log.warn("라운드 {} 결과 조회 실패: {}", round, e.getMessage());
                        return null;
                    }
                })
                .filter(result -> result != null)
                .toList();
    }

    private List<BalanceQuestionSummary> findMostBalancedQuestions(List<BalanceRoundResultResponse> roundResults, Long roomId) {
        // 가장 균형 잡힌 문제들 (50:50에 가까운 문제들)
        List<BalanceRoundResultResponse> nonTieResults = roundResults.stream()
                .filter(result -> !result.isTie())
                .toList();
                
        if (nonTieResults.isEmpty()) {
            return List.of();
        }
        
        // 50:50에 가장 가까운 차이 계산
        double minDifference = nonTieResults.stream()
                .mapToDouble(result -> Math.abs(result.choiceAPercentage() - 50.0))
                .min()
                .orElse(Double.MAX_VALUE);
        
        // 최소 차이와 동일한 문제들만 반환
        return nonTieResults.stream()
                .filter(result -> Math.abs(Math.abs(result.choiceAPercentage() - 50.0) - minDifference) < 0.01)
                .map(result -> createQuestionSummary(roomId, result.currentRound()))
                .toList();
    }

    private List<BalanceQuestionSummary> findMostUnanimousQuestions(List<BalanceRoundResultResponse> roundResults, Long roomId) {
        // 가장 만장일치에 가까운 문제들 (한쪽 선택지가 압도적인 문제들)
        List<BalanceRoundResultResponse> nonTieResults = roundResults.stream()
                .filter(result -> !result.isTie())
                .toList();
                
        if (nonTieResults.isEmpty()) {
            return List.of();
        }
        
        // 가장 높은 퍼센트 찾기
        double maxPercentage = nonTieResults.stream()
                .mapToDouble(result -> Math.max(result.choiceAPercentage(), result.choiceBPercentage()))
                .max()
                .orElse(0.0);
        
        // 최고 퍼센트와 동일한 문제들만 반환
        return nonTieResults.stream()
                .filter(result -> {
                    double maxPercent = Math.max(result.choiceAPercentage(), result.choiceBPercentage());
                    return Math.abs(maxPercent - maxPercentage) < 0.01;
                })
                .map(result -> createQuestionSummary(roomId, result.currentRound()))
                .toList();
    }
    
    private BalanceQuestionSummary createQuestionSummary(Long roomId, int round) {
        try {
            // 해당 라운드의 문제 조회 (display_order는 0-based이므로 -1)
            BalanceSessionQuestion question = balanceQuestionService.getCurrentQuestionByDisplayOrder(roomId, round - 1);
            return BalanceQuestionSummary.of(round, question.getQuestionA(), question.getQuestionB());
        } catch (Exception e) {
            log.warn("문제 조회 실패: round={}, error={}", round, e.getMessage());
            return BalanceQuestionSummary.of(round, "알 수 없음", "알 수 없음");
        }
    }
    
    private BalanceChoice getUserChoice(Long roomId, String memberName, Integer votingRound) {
        List<BalanceVote> votes = balanceVoteRepository.findByRoomIdAndVoterNameAndVotingRound(roomId, memberName, votingRound);
        if (votes.isEmpty()) {
            log.warn("사용자 투표를 찾을 수 없습니다: roomId={}, memberName={}, votingRound={}", roomId, memberName, votingRound);
            return BalanceChoice.A; // 기본값
        }
        return votes.get(0).getSelectedChoice();
    }
    
    private int calculateScoreChange(BalanceVoteResult voteResult, BalanceChoice userChoice) {
        if (voteResult.isTie()) {
            return 0; // 무승부일 때는 점수 변화 없음
        }
        
        if (userChoice == voteResult.majorityChoice()) {
            return 1; // 다수 선택과 일치하면 +1점
        } else {
            return -1; // 다수 선택과 다르면 -1점
        }
    }
} 