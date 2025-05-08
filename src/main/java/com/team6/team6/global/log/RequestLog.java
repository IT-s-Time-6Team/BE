package com.team6.team6.global.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static com.team6.team6.global.log.LogType.REQUEST;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestLog extends AbstractLog {
    private final Request request;
    private final Response response;
    private final long executionTimeMs;

    private RequestLog(Request request, Response response, long executionTimeMs) {
        super(REQUEST);
        this.request = request;
        this.response = response;
        this.executionTimeMs = executionTimeMs;
    }

    public static RequestLog create(ContentCachingRequestWrapper request,
                                    ContentCachingResponseWrapper response, long executionTimeMs) {
        return new RequestLog(
                new Request(request),
                new Response(response),
                executionTimeMs
        );
    }

    @Override
    public void output() {
        log.info(getLogMessage());
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Request {
        private final String uri;
        private final String method;
        private final String queryString;
        private final String body;
        private final Map<String, String> headers;

        public Request(ContentCachingRequestWrapper request) {
            this.uri = request.getRequestURI();
            this.method = request.getMethod();
            this.queryString = request.getQueryString();

            byte[] content = request.getContentAsByteArray();
            this.body = content != null && content.length > 0 ?
                    new String(content, StandardCharsets.UTF_8) : null;

            this.headers = extractHeaders(request);
        }

        private Map<String, String> extractHeaders(HttpServletRequest request) {
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
            return headers;
        }
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private final int statusCode;
        private final String body;
        private final Map<String, String> headers;

        public Response(ContentCachingResponseWrapper response) {
            this.statusCode = response.getStatus();
            this.body = new String(response.getContentAsByteArray());
            this.headers = extractHeaders(response);
        }

        private Map<String, String> extractHeaders(HttpServletResponse response) {
            Map<String, String> headers = new HashMap<>();
            for (String headerName : response.getHeaderNames()) {
                headers.put(headerName, response.getHeader(headerName));
            }
            return headers;
        }
    }
}
