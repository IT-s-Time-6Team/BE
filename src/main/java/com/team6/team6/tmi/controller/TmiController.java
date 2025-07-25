package com.team6.team6.tmi.controller;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.global.security.AuthUtil;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.tmi.dto.*;
import com.team6.team6.tmi.service.TmiHintService;
import com.team6.team6.tmi.service.TmiSessionService;
import com.team6.team6.tmi.service.TmiSubmitService;
import com.team6.team6.tmi.service.TmiVoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tmi")
@RequiredArgsConstructor
@Slf4j
public class TmiController {

    private final TmiVoteService tmiVoteService;
    private final TmiSubmitService tmiSubmitService;
    private final TmiSessionService tmiSessionService;
    private final TmiHintService tmiHintService;

    @PostMapping("/rooms/{roomKey}/submit")
    public ApiResponse<String> submitTmi(
            @PathVariable String roomKey,
            @Valid @RequestBody TmiSubmitRequest request) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("TMI 제출 요청: roomKey={}, tmi={}", roomKey, request.tmiContent());
        log.debug("사용자 정보: memberId={}, nickname={}, roomId={}",
                userPrincipal.getId(), userPrincipal.getNickname(), userPrincipal.getRoomId());

        // TMI 제출 처리
        tmiSubmitService.submitTmi(request.toServiceRequest(userPrincipal));

        log.debug("TMI 서비스 처리 완료: roomKey={}, memberName={}, content={}",
                roomKey, userPrincipal.getNickname(), request.tmiContent());

        return ApiResponse.of(HttpStatus.OK, "TMI가 성공적으로 제출되었습니다.");
    }

    @PostMapping("/rooms/{roomKey}/votes")
    public ApiResponse<String> submitVote(
            @PathVariable String roomKey,
            @Valid @RequestBody TmiVoteRequest request) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("TMI 투표 요청: roomKey={}, voter={}, voted={}",
                roomKey, userPrincipal.getNickname(), request.votedMemberName());

        tmiVoteService.submitVote(request.toServiceRequest(roomKey, userPrincipal));

        log.debug("TMI 투표 처리 완료: roomKey={}, voter={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.of(HttpStatus.OK, "투표가 성공적으로 제출되었습니다.");
    }

    @GetMapping("/rooms/{roomKey}/votes")
    public ApiResponse<TmiVotingStartResponse> getCurrentVotingInfo(
            @PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("현재 투표 정보 조회 요청: roomKey={}", roomKey);

        TmiVotingStartResponse response = tmiVoteService.getCurrentVotingInfo(userPrincipal.getRoomId());

        log.debug("현재 투표 정보 조회 완료: roomKey={}, tmiContent={}",
                roomKey, response.tmiContent());

        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms/{roomKey}/votes/result")
    public ApiResponse<TmiVotingPersonalResult> getLatestVotingResult(
            @PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("최신 투표 결과 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        TmiVotingPersonalResult result = tmiVoteService.getLatestVotingResult(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("최신 투표 결과 조회 완료: roomKey={}, memberName={}, isCorrect={}",
                roomKey, userPrincipal.getNickname(), result.isCorrect());

        return ApiResponse.ok(result);
    }

    @GetMapping("/rooms/{roomKey}/status")
    public ApiResponse<TmiSessionStatusResponse> getGameStatus(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("TMI 게임 상태 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        TmiSessionStatusResponse response = tmiSessionService.getSessionStatus(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("TMI 게임 상태 조회 완료: roomKey={}, currentStep={}, hasSubmitted={}",
                roomKey, response.currentStep(), response.hasUserSubmitted());

        return ApiResponse.ok(response);
    }

    @GetMapping("/rooms/{roomKey}/results")
    public ApiResponse<TmiSessionResultResponse> getSessionResults(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("TMI 게임 최종 결과 조회 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        TmiSessionResultResponse response = tmiSessionService.getSessionResults(
                userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("TMI 게임 최종 결과 조회 완료: roomKey={}, correctCount={}, incorrectCount={}",
                roomKey, response.correctCount(), response.incorrectCount());

        return ApiResponse.ok(response);
    }

    @PostMapping("/rooms/{roomKey}/hint/skip")
    public ApiResponse<String> skipHintTime(@PathVariable String roomKey) {
        UserPrincipal userPrincipal = AuthUtil.getCurrentUser();

        log.debug("힌트 타임 건너뛰기 요청: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        tmiHintService.skipHintTime(roomKey, userPrincipal.getRoomId(), userPrincipal.getNickname());

        log.debug("힌트 타임 건너뛰기 완료: roomKey={}, memberName={}",
                roomKey, userPrincipal.getNickname());

        return ApiResponse.of(HttpStatus.OK, "힌트 타임이 건너뛰어졌습니다.");
    }
}
