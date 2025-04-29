package com.team6.team6.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberCreateOrLoginRequest(
        @NotBlank(message = "닉네임을 입력해주세요")
        @Pattern(
            regexp = "^[a-zA-Z0-9가-힣]+$", 
            message = "닉네임은 특수문자와 이모티콘을 제외한 1글자 이상이어야 합니다"
        )
        @Size(min = 1, message = "닉네임은 최소 1글자 이상이어야 합니다")
        String nickname,
        
        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[!@#$%^&*(),.?\":{}|<>])(.{6,})$",
            message = "비밀번호는 영문 소문자, 특수 문자를 포함한 6글자 이상이어야 합니다"
        )
        @Size(min = 6, message = "비밀번호는 최소 6글자 이상이어야 합니다")
        String password
) {
    public MemberCreateOrLoginServiceRequest toServiceRequest() {
        return MemberCreateOrLoginServiceRequest.builder()
                .nickname(nickname)
                .password(password)
                .build();
    }
}