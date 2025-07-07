package com.team6.team6.global.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.team6.team6.global.log.LogMarker.REQUEST;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 파일 업로드 제외 (필요시)
        if (request.getContentType() != null &&
                request.getContentType().startsWith("multipart/form-data")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Prometheus 요청 제외
        if (request.getRequestURI().contains("/actuator/prometheus")) {
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
            logRequestResponse(requestWrapper, responseWrapper, duration);

            // 응답 바디 복원 (한 번만)
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request,
                                    ContentCachingResponseWrapper response,
                                    long executionTimeMs) {
        try {
            Map<String, Object> logData = new HashMap<>();

            // 요청 정보
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("uri", request.getRequestURI());
            requestData.put("method", request.getMethod());
            requestData.put("queryString", request.getQueryString());

            // 요청 바디
            byte[] content = request.getContentAsByteArray();
            if (content != null && content.length > 0) {
                requestData.put("body", new String(content, StandardCharsets.UTF_8));
            }

            // 요청 헤더
            Map<String, String> requestHeaders = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                requestHeaders.put(headerName, request.getHeader(headerName));
            }
            requestData.put("headers", requestHeaders);

            // 응답 정보
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statusCode", response.getStatus());
            responseData.put("body", new String(response.getContentAsByteArray(), StandardCharsets.UTF_8));

            // 응답 헤더
            Map<String, String> responseHeaders = new HashMap<>();
            for (String headerName : response.getHeaderNames()) {
                responseHeaders.put(headerName, response.getHeader(headerName));
            }
            responseData.put("headers", responseHeaders);

            // 전체 데이터 구성
            logData.put("request", requestData);
            logData.put("response", responseData);
            logData.put("executionTimeMs", executionTimeMs);

            // Marker를 사용하여 로그 출력
            String logMessage = objectMapper.writeValueAsString(logData);
            log.info(REQUEST.getMarker(), logMessage);
        } catch (Exception e) {
            log.error("로그 기록 중 오류 발생", e);
        }
    }
}
