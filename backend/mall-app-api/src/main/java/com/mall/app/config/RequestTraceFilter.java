package com.mall.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestTraceFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String SESSION_ID_HEADER = "X-Session-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveHeaderOrGenerate(request, TRACE_ID_HEADER, "trace");
        String requestId = resolveHeaderOrGenerate(request, REQUEST_ID_HEADER, "req");
        String sessionId = request.getHeader(SESSION_ID_HEADER);

        response.setHeader(TRACE_ID_HEADER, traceId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        if (sessionId != null && !sessionId.isBlank()) {
            response.setHeader(SESSION_ID_HEADER, sessionId);
        }

        MDC.put("traceId", traceId);
        MDC.put("requestId", requestId);
        if (sessionId != null && !sessionId.isBlank()) {
            MDC.put("sessionId", sessionId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            MDC.remove("requestId");
            MDC.remove("sessionId");
        }
    }

    private String resolveHeaderOrGenerate(HttpServletRequest request, String headerName, String prefix) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return prefix + "-" + UUID.randomUUID();
    }
}
