package com.team6.team6.tmi.controller;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.global.security.AuthUtil;
import com.team6.team6.member.security.UserPrincipal;
import com.team6.team6.tmi.dto.TmiSubmitRequest;
import com.team6.team6.tmi.dto.TmiVoteRequest;
import com.team6.team6.tmi.dto.TmiVotingPersonalResult;
import com.team6.team6.tmi.dto.TmiVotingStartResponse;
import com.team6.team6.tmi.service.TmiSubmitService;
import com.team6.team6.tmi.service.TmiVoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tmi")
@RequiredArgsConstructor
@Slf4j
public class TmiController {

    private final TmiVoteService tmiVoteService;
    private final TmiSubmitService tmiSubmitService;

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
}
