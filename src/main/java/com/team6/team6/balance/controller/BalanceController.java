package com.team6.team6.balance.controller;

import com.team6.team6.balance.dto.*;
import com.team6.team6.balance.service.BalanceDiscussionService;
import com.team6.team6.balance.service.BalanceResultService;
import com.team6.team6.balance.service.BalanceSessionService;
import com.team6.team6.balance.service.BalanceVoteService;
import com.team6.team6.global.ApiResponse;
import com.team6.team6.global.security.AuthUtil;
import com.team6.team6.member.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
@Slf4j
public class BalanceController {


    private final BalanceVoteService balanceVoteService;
    private final BalanceSessionService balanceSessionService;
    private final BalanceDiscussionService balanceDiscussionService;
    private final BalanceResultService balanceResultService;

    @GetMapping("/rooms/{roomKey}/status")
    public ApiResponse<BalanceSessionStatusResponse> getGameStatus(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("밸런스 게임 상태 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        BalanceSessionStatusResponse response = balanceSessionService.getSessionStatus(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("밸런스 게임 상태 조회 완료: roomKey={}, currentStep={}, hasSubmitted={}",
                roomKey, response.currentStep(), response.hasUserSubmitted());

        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms/{roomKey}/votes")
    public ApiResponse<BalanceVotingStartResponse> getCurrentVotingInfo(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("현재 질문 정보 조회 요청: roomKey={}", roomKey);

        BalanceVotingStartResponse response = balanceVoteService.getCurrentVotingInfo(userPrincipal.getRoomId());

        log.debug("현재 질문 정보 조회 완료: roomKey={}, questionA={}, questionB={}",
                roomKey, response.questionA(), response.questionB());

        return ApiResponse.ok(response);
    }

    @PostMapping("/rooms/{roomKey}/votes")
    public ApiResponse<String> submitVote(
            @PathVariable String roomKey,
            @Valid @RequestBody BalanceVoteRequest request) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("밸런스 투표 제출 요청: roomKey={}, choice={}, memberName={}",
                roomKey, request.selectedChoice(), userPrincipal.getNickname());

        // 서비스 요청 생성
        BalanceVoteServiceReq serviceReq = request.toServiceRequest(
                userPrincipal.getRoomId(),
                roomKey,
                userPrincipal.getId(),
                userPrincipal.getNickname()
        );

        balanceVoteService.submitVote(serviceReq);

        log.debug("밸런스 투표 제출 완료: roomKey={}, memberName={}, choice={}",
                roomKey, userPrincipal.getNickname(), request.selectedChoice());

        return ApiResponse.of(HttpStatus.OK, "투표가 성공적으로 제출되었습니다.");
    }

    @PostMapping("/rooms/{roomKey}/discussion/skip")
    public ApiResponse<String> skipDiscussion(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("토론 건너뛰기 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        balanceDiscussionService.skipDiscussion(roomKey, userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("토론 건너뛰기 완료: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.of(HttpStatus.OK, "토론이 건너뛰어졌습니다.");
    }

    @GetMapping("/rooms/{roomKey}/votes/result")
    public ApiResponse<BalanceRoundResultResponse> getLatestVotingResult(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("최신 투표 결과 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        BalanceRoundResultResponse response = balanceResultService.getLatestVotingResult(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("최신 투표 결과 조회 완료: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms/{roomKey}/results")
    public ApiResponse<BalanceFinalResultResponse> getSessionResults(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("밸런스 게임 최종 결과 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        BalanceFinalResultResponse response = balanceResultService.getSessionResults(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("밸런스 게임 최종 결과 조회 완료: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.ok(response);
    }

    @PostMapping("/rooms/{roomKey}/votes/ready")
    public ApiResponse<String> markResultViewReady(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("결과 확인 완료 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        balanceResultService.processGameReady(roomKey, userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("결과 확인 완료 처리 완료: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.of(HttpStatus.OK, "결과 확인이 완료되었습니다.");
    }


} 