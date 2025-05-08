package com.team6.team6.global.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static com.team6.team6.global.log.LogUtil.requestLog;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 파일 업로드 제외 (필요시)
        if (request.getContentType() != null &&
                request.getContentType().startsWith("multipart/form-data")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper =
                request instanceof ContentCachingRequestWrapper
                        ? (ContentCachingRequestWrapper) request
                        : new ContentCachingRequestWrapper(request);

        ContentCachingResponseWrapper responseWrapper =
                response instanceof ContentCachingResponseWrapper
                        ? (ContentCachingResponseWrapper) response
                        : new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 로깅 (응답이 완전히 생성된 후)
            long duration = System.currentTimeMillis() - startTime;
            requestLog(requestWrapper, responseWrapper, duration);

            // 응답 바디 복원 (한 번만)
            responseWrapper.copyBodyToResponse();
        }
    }
}
