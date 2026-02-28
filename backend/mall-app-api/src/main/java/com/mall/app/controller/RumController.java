package com.mall.app.controller;

import com.mall.common.api.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/rum")
public class RumController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RumController.class);

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, MediaType.ALL_VALUE})
    public CommonResult<String> collect(@RequestBody(required = false) String payload,
                                        @RequestHeader(value = "X-Trace-Id", required = false) String traceId,
                                        @RequestHeader(value = "X-Request-Id", required = false) String requestId,
                                        @RequestHeader(value = "X-Session-Id", required = false) String sessionId,
                                        HttpServletRequest request) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("traceId", traceId);
        event.put("requestId", requestId);
        event.put("sessionId", sessionId);
        event.put("method", request.getMethod());
        event.put("path", request.getRequestURI());
        event.put("userAgent", request.getHeader("User-Agent"));
        event.put("remoteAddr", request.getRemoteAddr());
        event.put("payload", payload);

        LOGGER.info("rum_event={}", event);
        return CommonResult.success("accepted");
    }
}
