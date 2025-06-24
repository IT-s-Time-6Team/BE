package com.team6.team6.global.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
public class LogUtil {

    // 정보 로그
    public static void infoLog(String message) {
        StandardLog.info(message).output();
    }

    // 에러 로그
    public static void errorLog(String message, Throwable exception) {
        StandardLog.error(message, exception).output();
    }

    // 디버그 로그
    public static void debugLog(String message) {
        StandardLog.debug(message).output();
    }

    // 경고 로그
    public static void warnLog(String message) {
        StandardLog.warn(message).output();
    }

    // 요청 로그
    public static void requestLog(ContentCachingRequestWrapper request,
                                  ContentCachingResponseWrapper response,
                                  long executionTimeMs) {
        if (request == null || response == null) {
            log.warn("Request or response is null, skipping request logging");
            return;
        }
        RequestLog.create(request, response, executionTimeMs).output();
    }
}
