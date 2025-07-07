package com.team6.team6.global.log;

import com.team6.team6.member.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class AuthInfoLoggingFilter extends OncePerRequestFilter {

    private static final String NICKNAME_KEY = "nickname";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof UserPrincipal principal) {
                MDC.put(NICKNAME_KEY, principal.getNickname());
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(NICKNAME_KEY);
        }
    }
}
