package com.team6.team6.member.controller;

import com.team6.team6.global.ApiResponse;
import com.team6.team6.member.dto.MemberCreateOrLoginRequest;
import com.team6.team6.member.dto.MemberResponse;
import com.team6.team6.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms/{roomKey}")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/member")
    public ApiResponse<MemberResponse> joinOrLogin(
            @PathVariable String roomKey,
            @Valid @RequestBody MemberCreateOrLoginRequest request) {
        MemberResponse response = memberService.joinOrLogin(roomKey, request.toServiceRequest());
        return ApiResponse.of(HttpStatus.OK,"인증 성공", response);
    }
}