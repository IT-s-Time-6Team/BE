package com.team6.team6.global.security;

import com.team6.team6.member.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {

    public static UserPrincipal getCurrentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        if (!(principal instanceof UserPrincipal)) {
            throw new IllegalStateException("올바르지 않은 인증 정보입니다.");
        }

        return (UserPrincipal) principal;
    }
}
